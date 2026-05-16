package com.nominal.lynx.infrastructure.output.persistence;

import com.nominal.lynx.domain.model.BatchRecord;
import com.nominal.lynx.domain.port.out.BatchRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MongoBatchAdapter — Implementación del Puerto de Salida con MongoDB 🔌🍃
 *
 * Esta clase es el "adaptador" que conecta el Dominio (puro Java)
 * con MongoDB (infraestructura técnica).
 *
 * Responsabilidades:
 * ─────────────────
 * 1. Implementar BatchRepositoryPort (puerto del dominio).
 * 2. Delegar las operaciones a BatchMongoRepository (Spring Data).
 * 3. Convertir entre BatchRecord (dominio) ↔ BatchDocument (infraestructura).
 *
 * Arquitectura Hexagonal en acción:
 * ────────────────────────────────
 * El Dominio NO conoce MongoDB. Solo conoce BatchRepositoryPort (interface).
 * Este Adapter es el puente. Si mañana migramos a PostgreSQL, solo
 * cambiamos esta clase; el Dominio permanece intacto.
 *
 * @Component: Spring lo detecta y lo registra como Bean.
 * El Use Case recibe BatchRepositoryPort (interface) por inyección,
 * y Spring le entrega esta implementación concreta.
 */
@Component
public class MongoBatchAdapter implements BatchRepositoryPort {

    private final BatchMongoRepository repository;

    /**
     * Inyección por constructor (la única forma correcta en Clean Architecture).
     * Spring busca un Bean de tipo BatchMongoRepository y lo inyecta.
     */
    public MongoBatchAdapter(BatchMongoRepository repository) {
        this.repository = repository;
    }

    // =========================================================================
    // IMPLEMENTACIÓN DEL PUERTO: BatchRepositoryPort
    // =========================================================================

    @Override
    public BatchRecord save(BatchRecord batch) {
        // 1. Convertir Record del dominio → Document de Mongo
        BatchDocument document = BatchDocument.fromDomain(batch);

        // 2. Guardar en MongoDB (Spring Data hace la magia)
        BatchDocument saved = repository.save(document);

        // 3. Convertir Document de Mongo → Record del dominio
        return saved.toDomain();
    }

    @Override
    public Optional<BatchRecord> findById(UUID id) {
        return repository.findById(id)
                .map(BatchDocument::toDomain);  // Mapear de Document a Record
    }

    @Override
    public List<BatchRecord> findBySku(String sku) {
        return repository.findBySku(sku)
                .stream()
                .map(BatchDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<BatchRecord> findByStatus(BatchRecord.BatchStatus status) {
        // Convertir Enum → String para la consulta
        String statusString = status.name();
        return repository.findByCurrentStatus(statusString)
                .stream()
                .map(BatchDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<BatchRecord> findAll() {
        return repository.findAll()
                .stream()
                .map(BatchDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        // ⚠️ HARD DELETE — Solo para testing
        repository.deleteById(id);
    }
}