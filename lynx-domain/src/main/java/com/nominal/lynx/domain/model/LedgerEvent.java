package com.nominal.lynx.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * LedgerEvent — Evento del Ledger (Libro Mayor) 📖
 *
 * Representa un cambio inmutable en la auditoría de LYNX.
 * El Ledger es append-only: solo se pueden añadir eventos, nunca borrar o editar.
 *
 * Decisiones de diseño:
 * ────────────────────
 * 1. Java 17 Record: Inmutabilidad garantizada por compilador.
 *    Una vez creado, un LedgerEvent no puede cambiar jamás.
 *
 * 2. eventId (UUID): Cada evento es único y trazable.
 *    Permite buscar "¿qué cambió el 2026-05-10 a las 14:32?"
 *
 * 3. batchId: Referencia al lote afectado.
 *    Pregunta: "¿Cuál es el historial del lote #xyz?"
 *    Respuesta: "Todos los LedgerEvents donde batchId == xyz, ordenados por timestamp"
 *
 * 4. metadata (Map<String, Object>): Shadow Schema para detalles dinámicos.
 *    Ejemplos:
 *    - {"previousStatus": "DRAFT", "newStatus": "ACTIVE", "reason": "Revisado por QA"}
 *    - {"ph": 7.2, "salinidad": 35.1, "zona": "Marisma Norte"}
 *
 * 5. timestamp (Instant): Cuándo sucedió, en UTC.
 *    Nunca debe ser en el futuro. Validado en constructor compacto.
 *
 * Regla de oro:
 * ─────────────
 * El Ledger es la fuente de verdad. Si algo no está aquí, no sucedió.
 * Si necesitas "deshacer" un cambio, añades un nuevo evento (ROLLBACK o CORRECTION),
 * nunca borras el anterior.
 */
public record LedgerEvent(
        UUID eventId,
        UUID batchId,
        LedgerAction action,
        EventSource source,
        String description,
        Map<String, Object> metadata,
        Instant timestamp
) {

    // =========================================================================
    // COMPACT CONSTRUCTOR — Validación en construcción
    // =========================================================================
    public LedgerEvent {
        if (eventId == null)    throw new IllegalArgumentException("eventId no puede ser nulo.");
        if (batchId == null)    throw new IllegalArgumentException("batchId no puede ser nulo.");
        if (action == null)     throw new IllegalArgumentException("action no puede ser nula.");
        if (source == null)     throw new IllegalArgumentException("source no puede ser nula.");
        if (description == null || description.isBlank())
            throw new IllegalArgumentException("description es obligatoria y no puede estar en blanco.");
        if (timestamp == null)  throw new IllegalArgumentException("timestamp no puede ser nulo.");
        if (timestamp.isAfter(Instant.now()))
            throw new IllegalArgumentException("timestamp no puede estar en el futuro (anomalía de reloj de servidor).");

        // Normalizar metadata a Map inmutable
        metadata = (metadata != null) ? Map.copyOf(metadata) : Map.of();
    }

    // =========================================================================
    // FACTORY METHODS — Puntos de entrada canónicos por tipo de evento
    // =========================================================================

    /**
     * Crea un evento CREATE: "Se creó un nuevo lote"
     */
    public static LedgerEvent ofCreate(
            UUID batchId,
            EventSource source,
            String sku,
            double quantity,
            UUID userId
    ) {
        return new LedgerEvent(
                UUID.randomUUID(),
                batchId,
                LedgerAction.CREATE,
                source,
                "Lote creado: " + sku + " (" + quantity + " unidades)",
                Map.of("sku", sku, "quantity", quantity, "userId", userId),
                Instant.now()
        );
    }

    /**
     * Crea un evento STATUS_CHANGE: "El lote cambió de estado"
     */
    public static LedgerEvent ofStatusChange(
            UUID batchId,
            EventSource source,
            BatchRecord.BatchStatus previousStatus,
            BatchRecord.BatchStatus newStatus,
            String reason,
            UUID userId
    ) {
        return new LedgerEvent(
                UUID.randomUUID(),
                batchId,
                LedgerAction.STATUS_CHANGE,
                source,
                "Estado cambió de " + previousStatus + " a " + newStatus,
                Map.of(
                        "previousStatus", previousStatus.name(),
                        "newStatus", newStatus.name(),
                        "reason", reason != null ? reason : "Sin especificar",
                        "userId", userId
                ),
                Instant.now()
        );
    }

    /**
     * Crea un evento QUANTITY_UPDATE: "Se actualizó la cantidad del lote"
     */
    public static LedgerEvent ofQuantityUpdate(
            UUID batchId,
            EventSource source,
            double previousQuantity,
            double newQuantity,
            String reason,
            UUID userId
    ) {
        return new LedgerEvent(
                UUID.randomUUID(),
                batchId,
                LedgerAction.QUANTITY_UPDATE,
                source,
                "Cantidad actualizada de " + previousQuantity + " a " + newQuantity,
                Map.of(
                        "previousQuantity", previousQuantity,
                        "newQuantity", newQuantity,
                        "reason", reason != null ? reason : "Sin especificar",
                        "userId", userId
                ),
                Instant.now()
        );
    }

    /**
     * Crea un evento QUALITY_CHECK: "Se realizó un control de calidad"
     */
    public static LedgerEvent ofQualityCheck(
            UUID batchId,
            EventSource source,
            String parameter,
            double value,
            String status, // "OK" o "FAIL"
            UUID userId
    ) {
        return new LedgerEvent(
                UUID.randomUUID(),
                batchId,
                LedgerAction.QUALITY_CHECK,
                source,
                "Control de calidad: " + parameter + "=" + value + " (" + status + ")",
                Map.of(
                        "parameter", parameter,
                        "value", value,
                        "status", status,
                        "userId", userId
                ),
                Instant.now()
        );
    }

    /**
     * Crea un evento DISPATCH: "El lote fue enviado"
     */
    public static LedgerEvent ofDispatch(
            UUID batchId,
            EventSource source,
            String destination,
            String carrier,
            UUID userId
    ) {
        return new LedgerEvent(
                UUID.randomUUID(),
                batchId,
                LedgerAction.DISPATCH,
                source,
                "Lote enviado a " + destination + " con " + carrier,
                Map.of(
                        "destination", destination,
                        "carrier", carrier,
                        "userId", userId
                ),
                Instant.now()
        );
    }

    // =========================================================================
    // MÉTODOS DE CONSULTA — Para inspeccionar el evento desde el Dominio
    // =========================================================================

    /**
     * ¿Este evento fue originado por un usuario?
     */
    public boolean isUserOriginated() {
        return source == EventSource.USER;
    }

    /**
     * ¿Este evento fue originado por un sensor IoT?
     */
    public boolean isIoTOriginated() {
        return source == EventSource.IOT_SENSOR;
    }

    /**
     * ¿Este evento fue originado por el sistema?
     */
    public boolean isSystemOriginated() {
        return source == EventSource.SYSTEM;
    }

    /**
     * ¿Este evento es un cambio de estado?
     */
    public boolean isStatusChange() {
        return action == LedgerAction.STATUS_CHANGE;
    }

    /**
     * Extrae un valor de metadata de forma segura.
     * Si no existe, devuelve null (no lanza excepción).
     */
    public Object getMetadataValue(String key) {
        return metadata.getOrDefault(key, null);
    }

    /**
     * Extrae un String de metadata.
     */
    public String getMetadataString(String key) {
        Object value = getMetadataValue(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Extrae un Double de metadata.
     */
    public Double getMetadataDouble(String key) {
        Object value = getMetadataValue(key);
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return null;
    }
}