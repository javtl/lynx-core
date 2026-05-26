package com.nominal.lynx.application.port.in;

import com.nominal.lynx.application.dto.BatchResponse;
import com.nominal.lynx.domain.port.out.BatchRepositoryPort;
import java.util.UUID;

public class GetBatchUseCaseImpl implements GetBatchUseCase {

    private final BatchRepositoryPort batchRepository;

    // Constructor para la inyección manual que hacemos en UseCaseBeans
    public GetBatchUseCaseImpl(BatchRepositoryPort batchRepository) {
        this.batchRepository = batchRepository;
    }

    @Override
    public BatchResponse execute(UUID batchId) {
        // TODO: Aquí irá la lógica de lectura (buscar en repositorio, lanzar excepción si no existe, mapear a respuesta)
        return null;
    }
}