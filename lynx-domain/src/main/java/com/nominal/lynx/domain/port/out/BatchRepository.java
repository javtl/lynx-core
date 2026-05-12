package com.nominal.lynx.domain.port.out;

import com.nominal.lynx.domain.model.BatchRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * BatchRepository — Puerto de Salida (Output Port)
 *
 * Este interface define el CONTRATO de persistencia desde el punto de
 * vista del Dominio. El Dominio declara QUÉ necesita; la Infrastructure
 * decide CÓMO implementarlo (MongoDB, PostgreSQL, in-memory para tests...).
 *
 * Regla de Arquitectura Hexagonal:
 * ─────────────────────────────────
 * ✅ Este interface VIVE en lynx-domain.
 * ✅ Su IMPLEMENTACIÓN (BatchMongoAdapter) vive en lynx-infrastructure.
 * ❌ NUNCA importes Spring Data o MongoDB aquí.
 *
 * El test de dominio puede crear un "FakeBatchRepository" que implemente
 * este contrato con un simple HashMap, sin levantar Spring ni Mongo.
 */
public interface BatchRepository {

    /**
     * Persists a batch snapshot.
     */
    BatchRecord save(BatchRecord batch);

    /**
     * Retrieves a batch by its identifier.
     */
    Optional<BatchRecord> findById(UUID id);

    /**
     * Finds batches by product SKU.
     */
    List<BatchRecord> findBySku(String sku);

    /**
     * Lists all known batches.
     */
    List<BatchRecord> findAll();

    /**
     * HARD DELETE está prohibido en LYNX por diseño.
     * El Ledger es inmutable. Si necesitas "cancelar" un lote,
     * usa transitionTo(DISCARDED) + registra el LedgerEvent.
     *
     * Este método existe solo para el entorno de tests de integración.
     */
    void hardDeleteForTestingOnly(UUID id);
}
