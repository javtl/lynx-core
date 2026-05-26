package com.nominal.lynx.application.port.in;

import com.nominal.lynx.application.dto.CreateBatchRequest;
import com.nominal.lynx.application.dto.BatchResponse;
import com.nominal.lynx.domain.port.out.BatchRepositoryPort;

public class CreateBatchUseCaseImpl implements CreateBatchUseCase {

    private final BatchRepositoryPort batchRepository;

    // Constructor para la inyección manual que hacemos en UseCaseBeans
    public CreateBatchUseCaseImpl(BatchRepositoryPort batchRepository) {
        this.batchRepository = batchRepository;
    }

    @Override
    public BatchResponse execute(CreateBatchRequest request) {
        // TODO: Aquí irá la lógica de negocio (mapear request a dominio, guardar en repositorio, etc.)
        return null;
    }
}