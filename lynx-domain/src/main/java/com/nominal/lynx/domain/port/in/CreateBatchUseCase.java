package com.nominal.lynx.domain.port.in;

import com.nominal.lynx.domain.model.BatchRecord;

import java.util.Map;
import java.util.UUID;

/**
 * Input port for creating new batches from the application layer.
 */
public interface CreateBatchUseCase {

    /**
     * Creates and persists a draft batch.
     */
    BatchRecord create(String sku, double quantity, String unit, Map<String, Object> metadata, UUID createdBy);
}
