package com.nominal.lynx.infrastructure.output.persistence;

import com.nominal.lynx.domain.model.BatchRecord;
import com.nominal.lynx.domain.port.out.BatchRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Output adapter for batch persistence.
 * <p>
 * Current implementation is in-memory to keep bootstrap lightweight;
 * it can be replaced with MongoTemplate/Spring Data without touching domain/application.
 */
@Component
public class BatchMongoAdapter implements BatchRepository {

    private final Map<UUID, BatchRecord> store = new HashMap<>();

    @Override
    public BatchRecord save(BatchRecord batch) {
        store.put(batch.id(), batch);
        return batch;
    }

    @Override
    public Optional<BatchRecord> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<BatchRecord> findBySku(String sku) {
        return store.values()
                .stream()
                .filter(batch -> batch.sku().equals(sku))
                .toList();
    }

    @Override
    public List<BatchRecord> findAll() {
        return store.values().stream().toList();
    }

    @Override
    public void hardDeleteForTestingOnly(UUID id) {
        store.remove(id);
    }
}
