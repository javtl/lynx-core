package com.nominal.lynx.domain.model;

/**
 * EventSource — Catálogo de orígenes de eventos 🔍
 *
 * Define QUIÉN o QUÉ originó el evento. Es la pregunta: ¿Fue un usuario,
 * el sistema, una API externa, o un sensor IoT?
 *
 * Regla de Dominio:
 * ─────────────────
 * Todo evento debe tener un origen trazable. Si no sabes de dónde vino,
 * no puedes auditarlo. Por eso EventSource es obligatorio en LedgerEvent.
 */
public enum EventSource {

    /**
     * USER: Un usuario humano realizó la acción (Admin, Operario, Auditor).
     * Cuándo: Login en el sistema + acción manual.
     * Trazabilidad: El userId está en el evento, sabemos quién fue.
     * Ejemplo: "Admin Juan PATCH /batches/123/status → ACTIVE"
     */
    USER("Usuario humano"),

    /**
     * SYSTEM: El sistema automático realizó la acción (jobs, inicializaciones).
     * Cuándo: Tareas programadas sin intervención manual.
     * Trazabilidad: El evento tiene un jobId o scheduleName.
     * Ejemplo: "Sistema marcó como DISCARDED los lotes vencidos"
     */
    SYSTEM("Sistema automático"),

    /**
     * API: Un cliente externo (otro sistema, integración) realizó la acción.
     * Cuándo: POST/PATCH desde un webhook, n8n, Zapier, etc.
     * Trazabilidad: El apiKeyId o partnerId está en el evento.
     * Ejemplo: "API Key xyz POST /batches → lote creado"
     */
    API("Integración externa"),

    /**
     * IOT_SENSOR: Un sensor IoT reportó un dato (temperatura, pH, humedad).
     * Cuándo: Lectura automática de dispositivo.
     * Trazabilidad: El deviceId y su geolocalización están en metadata.
     * Ejemplo: "Sensor 42 reportó pH=7.2 en lote #999"
     */
    IOT_SENSOR("Sensor IoT");

    private final String description;

    EventSource(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}