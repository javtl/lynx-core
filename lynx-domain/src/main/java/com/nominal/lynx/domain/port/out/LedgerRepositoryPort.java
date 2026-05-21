package com.nominal.lynx.domain.port.out;

import com.nominal.lynx.domain.model.LedgerEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * LedgerRepositoryPort — Puerto de Salida para el Ledger (Libro Mayor) 📖🔌
 *
 * Este interface define el contrato de persistencia del historial de auditoría.
 * El Ledger es APPEND-ONLY: solo se pueden añadir eventos, nunca borrar ni editar.
 *
 * Regla de oro del Ledger:
 * ────────────────────────
 * No existe un método update() ni delete() aquí. Si un evento se guardó mal,
 * se añade un nuevo evento (CORRECTION) que lo aclare, pero el original
 * permanece intacto. Esa es la esencia de la inmutabilidad del Ledger.
 *
 * Arquitectura Hexagonal:
 * ───────────────────────
 * ✅ Este interface VIVE en lynx-domain.
 * ✅ Su IMPLEMENTACIÓN (MongoLedgerAdapter) vive en lynx-infrastructure.
 * ❌ NUNCA importes anotaciones de Spring Data, MongoDB, JPA aquí.
 */
public interface LedgerRepositoryPort {

    /**
     * Añade un nuevo evento al Ledger.
     * Este es el método crítico del sistema. Cada cambio en un lote
     * debe generar un evento aquí.
     *
     * Regla crítica:
     * ──────────────
     * Si este método falla, la transacción que modificó el lote debe revertirse.
     * Eso se garantiza con @Transactional en la capa de Application/Use Case.
     *
     * @param event Evento inmutable a registrar (Record del dominio)
     * @return El evento guardado (con el mismo eventId)
     */
    LedgerEvent append(LedgerEvent event);

    /**
     * Recupera un evento específico por su ID.
     * Útil para auditorías: "¿Qué pasó en el evento abc-123?"
     *
     * @param eventId UUID del evento
     * @return Optional con el evento si existe
     */
    Optional<LedgerEvent> findById(UUID eventId);

    /**
     * Recupera TODOS los eventos de un lote específico, ordenados cronológicamente.
     * Esta es la consulta más común: "Dame el historial del lote #xyz"
     *
     * Orden: Del más antiguo al más reciente (timestamp ascendente).
     *
     * @param batchId UUID del lote
     * @return Lista de eventos (puede estar vacía si el lote es nuevo)
     */
    List<LedgerEvent> findByBatchId(UUID batchId);

    /**
     * Recupera los eventos de un lote en un rango de tiempo específico.
     * Útil para auditorías: "¿Qué pasó con el lote #xyz entre el 1 y el 15 de mayo?"
     *
     * @param batchId UUID del lote
     * @param from    Timestamp inicial (inclusive)
     * @param to      Timestamp final (inclusive)
     * @return Lista de eventos en ese rango temporal
     */
    List<LedgerEvent> findByBatchIdAndTimestampBetween(UUID batchId, Instant from, Instant to);

    /**
     * Recupera los N eventos más recientes de un lote.
     * Útil para UI: "Muestra los últimos 10 cambios del lote"
     *
     * @param batchId UUID del lote
     * @param limit   Número máximo de eventos a devolver
     * @return Lista de eventos (máximo 'limit' elementos)
     */
    List<LedgerEvent> findRecentByBatchId(UUID batchId, int limit);

    /**
     * Cuenta cuántos eventos tiene un lote.
     * Métrica de auditoría: "El lote #xyz tiene 47 eventos registrados"
     *
     * @param batchId UUID del lote
     * @return Número de eventos
     */
    long countByBatchId(UUID batchId);

    /**
     * Recupera todos los eventos del sistema (para análisis global).
     * ⚠️ ADVERTENCIA: En producción con miles de lotes, esto puede ser muy lento.
     * Usar con paginación en capa de infraestructura.
     *
     * @return Lista completa de eventos (ordenados por timestamp)
     */
    List<LedgerEvent> findAll();

    /**
     * HARD DELETE — Solo para testing.
     * ⚠️ NUNCA usar en producción.
     * El Ledger es sagrado; borrarlo rompe la auditoría.
     *
     * @param eventId UUID del evento a borrar
     */
    void deleteById(UUID eventId);

    /**
     * HARD DELETE BATCH — Solo para testing.
     * Borra todos los eventos de un lote específico.
     * ⚠️ NUNCA usar en producción.
     *
     * @param batchId UUID del lote
     */
    void deleteAllByBatchId(UUID batchId);
}