package com.nominal.lynx.infrastructure.output.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * LedgerEventMongoRepository — Repositorio Spring Data para el Ledger 📖🍃
 *
 * Consultas personalizadas para auditorías y trazabilidad.
 */
@Repository
public interface LedgerEventMongoRepository extends MongoRepository<LedgerEventDocument, UUID> {

    /**
     * Todos los eventos de un lote, ordenados cronológicamente.
     * Query: { "batch_id": UUID } ORDER BY timestamp ASC
     */
    List<LedgerEventDocument> findByBatchIdOrderByTimestampAsc(UUID batchId);

    /**
     * Eventos de un lote en un rango temporal.
     */
    List<LedgerEventDocument> findByBatchIdAndTimestampBetween(UUID batchId, Instant from, Instant to);

    /**
     * Los N eventos más recientes de un lote.
     */
    List<LedgerEventDocument> findByBatchIdOrderByTimestampDesc(UUID batchId, Pageable pageable);

    /**
     * Cuenta eventos de un lote.
     */
    long countByBatchId(UUID batchId);

    /**
     * Borra todos los eventos de un lote (solo testing).
     */
    void deleteAllByBatchId(UUID batchId);
}