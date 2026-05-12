package com.nominal.lynx.application.usecase;

import com.nominal.lynx.application.dto.CreateBatchCommand;
import com.nominal.lynx.domain.model.BatchRecord;
import com.nominal.lynx.domain.port.in.CreateBatchUseCase;
import com.nominal.lynx.domain.port.out.BatchRepository;

/**
 * Application service that orchestrates batch creation.
 * <p>
 * It delegates business rules to the domain model and persistence to an output port.
 */
public class CreateBatchService implements CreateBatchUseCase {

    private final BatchRepository batchRepository;

    public CreateBatchService(BatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    @Override
    public BatchRecord create(
            String sku,
            double quantity,
            String unit,
            java.util.Map<String, Object> metadata,
            java.util.UUID createdBy
    ) {
        BatchRecord batch = BatchRecord.create(sku, quantity, unit, metadata, createdBy);
        return batchRepository.save(batch);
    }

    public BatchRecord create(CreateBatchCommand command) {
        return create(
                command.sku(),
                command.quantity(),
                command.unit(),
                command.metadata(),
                command.createdBy()
        );
    }
}
