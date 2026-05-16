package com.nominal.lynx.infrastructure.output.persistence;

import com.nominal.lynx.domain.model.LedgerEvent;
import com.nominal.lynx.domain.port.out.LedgerRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MongoLedgerAdapter — Adaptador del Ledger a MongoDB 🔌📖
 *
 * Implementa el puerto LedgerRepositoryPort usando Spring Data.
 * Convierte entre LedgerEvent (dominio) ↔ LedgerEventDocument (infraestructura).
 */
@Component
public class MongoLedgerAdapter implements LedgerRepositoryPort {

    private final LedgerEventMongoRepository repository;

    public MongoLedgerAdapter(LedgerEventMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public LedgerEvent append(LedgerEvent event) {
        LedgerEventDocument document = LedgerEventDocument.fromDomain(event);
        LedgerEventDocument saved = repository.save(document);
        return saved.toDomain();
    }

    @Override
    public Optional<LedgerEvent> findById(UUID eventId) {
        return repository.findById(eventId)
                .map(LedgerEventDocument::toDomain);
    }

    @Override
    public List<LedgerEvent> findByBatchId(UUID batchId) {
        return repository.findByBatchIdOrderByTimestampAsc(batchId)
                .stream()
                .map(LedgerEventDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LedgerEvent> findByBatchIdAndTimestampBetween(UUID batchId, Instant from, Instant to) {
        return repository.findByBatchIdAndTimestampBetween(batchId, from, to)
                .stream()
                .map(LedgerEventDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LedgerEvent> findRecentByBatchId(UUID batchId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return repository.findByBatchIdOrderByTimestampDesc(batchId, pageRequest)
                .stream()
                .map(LedgerEventDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByBatchId(UUID batchId) {
        return repository.countByBatchId(batchId);
    }

    @Override
    public List<LedgerEvent> findAll() {
        return repository.findAll()
                .stream()
                .map(LedgerEventDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID eventId) {
        repository.deleteById(eventId);
    }

    @Override
    public void deleteAllByBatchId(UUID batchId) {
        repository.deleteAllByBatchId(batchId);
    }
}