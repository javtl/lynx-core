package com.nominal.lynx.infrastructure.output.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * BatchMongoRepository — Repositorio de Spring Data para Batches 🍃
 *
 * Este interface extiende MongoRepository, lo que le da GRATIS todos los
 * métodos básicos de CRUD: save(), findById(), findAll(), delete(), etc.
 *
 * Aquí solo añadimos las consultas personalizadas que LYNX necesita.
 *
 * Magia de Spring Data:
 * ────────────────────
 * Spring Data genera la implementación automáticamente al interpretar
 * el nombre del método. "findBySku" se traduce a una query de MongoDB:
 * db.batches.find({ "sku": "SALICORNIA-001" })
 *
 * Naming Convention:
 * ─────────────────
 * findBy<Campo>       → Busca por un campo específico
 * findBy<Campo>And<OtroCampo> → Consulta con múltiples condiciones
 * countBy<Campo>      → Cuenta documentos que cumplan la condición
 * existsBy<Campo>     → Devuelve true/false si existe
 *
 * IMPORTANTE:
 * ──────────
 * Este repositorio trabaja con BatchDocument (infraestructura).
 * El Adapter (MongoBatchAdapter) es responsable de convertir entre
 * BatchDocument ↔ BatchRecord.
 */
@Repository
public interface BatchMongoRepository extends MongoRepository<BatchDocument, UUID> {

    /**
     * Busca todos los lotes de un producto específico (por SKU).
     * Query generada: { "sku": "SALICORNIA-001" }
     *
     * @param sku Código del producto
     * @return Lista de documentos (puede estar vacía)
     */
    List<BatchDocument> findBySku(String sku);

    /**
     * Busca todos los lotes en un estado específico.
     * Query generada: { "current_status": "ACTIVE" }
     *
     * @param currentStatus Estado del ciclo de vida (como String)
     * @return Lista de documentos (puede estar vacía)
     */
    List<BatchDocument> findByCurrentStatus(String currentStatus);

    /**
     * Busca un lote por SKU y estado (consulta compuesta).
     * Útil para: "Dame todos los lotes ACTIVOS de SALICORNIA-001"
     *
     * Query generada: { "sku": "SALICORNIA-001", "current_status": "ACTIVE" }
     *
     * @param sku           Código del producto
     * @param currentStatus Estado
     * @return Lista de documentos
     */
    List<BatchDocument> findBySkuAndCurrentStatus(String sku, String currentStatus);

    /**
     * Verifica si existe algún lote con este SKU.
     * Útil para validaciones: "¿Ya existe un lote con este código?"
     *
     * @param sku Código del producto
     * @return true si existe al menos uno, false si no
     */
    boolean existsBySku(String sku);
}
