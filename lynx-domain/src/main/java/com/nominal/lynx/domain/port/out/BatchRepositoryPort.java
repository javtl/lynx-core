package com.nominal.lynx.domain.port.out;

import com.nominal.lynx.domain.model.BatchRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * BatchRepositoryPort — Puerto de Salida para persistencia de Lotes 🔌
 *
 * Este interface define el CONTRATO de persistencia desde el punto de vista
 * del Dominio. El dominio declara QUÉ necesita; la infraestructura (MongoDB)
 * decide CÓMO implementarlo.
 *
 * Regla de Arquitectura Hexagonal:
 * ─────────────────────────────────
 * ✅ Este interface VIVE en lynx-domain (puerto de salida).
 * ✅ Su IMPLEMENTACIÓN (MongoBatchAdapter) vive en lynx-infrastructure.
 * ❌ NUNCA importes Spring Data, MongoDB, JPA, o cualquier framework aquí.
 *
 * El test de dominio puede crear un "FakeBatchRepository" que implemente
 * este contrato con un HashMap, sin levantar Spring ni MongoDB.
 *
 * IMPORTANTE sobre HARD DELETE:
 * ─────────────────────────────
 * El método deleteById() existe SOLO para testing. En LYNX, el Ledger es
 * inmutable: si un lote debe "cancelarse", se hace con transitionTo(DISCARDED)
 * + un LedgerEvent que lo registre. Nunca se borra físicamente.
 */
public interface BatchRepositoryPort {

    /**
     * Guarda o actualiza un lote.
     * Si el ID ya existe, actualiza; si no, crea nuevo.
     *
     * @param batch El lote a persistir (Record inmutable del dominio)
     * @return El lote guardado (con el mismo ID)
     */
    BatchRecord save(BatchRecord batch);

    /**
     * Busca un lote por su ID único.
     *
     * @param id UUID del lote
     * @return Optional con el lote si existe, vacío si no
     */
    Optional<BatchRecord> findById(UUID id);

    /**
     * Busca todos los lotes de un producto específico (por SKU).
     * Útil para consultas de inventario: "Dame todos los lotes de SALICORNIA-001"
     *
     * @param sku Código del producto
     * @return Lista de lotes (puede estar vacía)
     */
    List<BatchRecord> findBySku(String sku);

    /**
     * Busca todos los lotes que coincidan con un estado específico.
     * Ejemplo: "Dame todos los lotes en ACTIVE"
     *
     * @param status Estado del ciclo de vida
     * @return Lista de lotes en ese estado
     */
    List<BatchRecord> findByStatus(BatchRecord.BatchStatus status);

    /**
     * Recupera TODOS los lotes del sistema.
     * ADVERTENCIA: En producción con miles de lotes, esto puede ser lento.
     * Usar con paginación en capa de infraestructura.
     *
     * @return Lista completa de lotes
     */
    List<BatchRecord> findAll();

    /**
     * Cuenta cuántos lotes existen en total.
     * Útil para dashboards: "Total de lotes: 347"
     *
     * @return Número de lotes
     */
    long count();

    /**
     * Verifica si un lote con este ID existe.
     *
     * @param id UUID del lote
     * @return true si existe, false si no
     */
    boolean existsById(UUID id);

    /**
     * HARD DELETE — Solo para testing.
     * ⚠️ NUNCA usar en producción.
     * El Ledger de LYNX es inmutable; no se borran lotes físicamente.
     *
     * @param id UUID del lote a borrar
     */
    void deleteById(UUID id);
}
