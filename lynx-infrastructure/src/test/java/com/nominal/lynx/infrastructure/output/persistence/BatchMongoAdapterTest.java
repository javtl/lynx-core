package com.nominal.lynx.infrastructure.output.persistence;

import com.nominal.lynx.domain.model.BatchRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchMongoAdapterTest {

    private BatchMongoAdapter adapter;
    private UUID creatorId;

    @BeforeEach
    void setUp() {
        adapter = new BatchMongoAdapter();
        creatorId = UUID.randomUUID();
    }

    @Test
    void saveAndFindById_shouldReturnPersistedBatch() {
        BatchRecord batch = BatchRecord.create("SKU-INF-001", 8.0, "kg", Map.of("zone", "A"), creatorId);
        adapter.save(batch);

        assertTrue(adapter.findById(batch.id()).isPresent());
        assertEquals("SKU-INF-001", adapter.findById(batch.id()).orElseThrow().sku());
    }

    @Test
    void findBySku_shouldFilterBatches() {
        BatchRecord one = BatchRecord.create("SKU-INF-002", 2.0, "kg", Map.of(), creatorId);
        BatchRecord two = BatchRecord.create("SKU-INF-002", 3.0, "kg", Map.of(), creatorId);
        BatchRecord other = BatchRecord.create("SKU-INF-003", 4.0, "kg", Map.of(), creatorId);

        adapter.save(one);
        adapter.save(two);
        adapter.save(other);

        assertEquals(2, adapter.findBySku("SKU-INF-002").size());
    }

    @Test
    void findAllAndDelete_shouldReflectStoreChanges() {
        BatchRecord batch = BatchRecord.create("SKU-INF-004", 1.0, "kg", Map.of(), creatorId);
        adapter.save(batch);
        assertEquals(1, adapter.findAll().size());

        adapter.hardDeleteForTestingOnly(batch.id());
        assertFalse(adapter.findById(batch.id()).isPresent());
        assertEquals(0, adapter.findAll().size());
    }
}
