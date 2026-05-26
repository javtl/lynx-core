package com.nominal.lynx.application.port.in;

import com.nominal.lynx.application.dto.CreateBatchRequest;
import com.nominal.lynx.application.dto.BatchResponse;
import com.nominal.lynx.domain.port.out.BatchRepositoryPort;
import java.util.UUID;
import java.util.Map;

public class CreateBatchUseCaseImpl implements CreateBatchUseCase {

    private final BatchRepositoryPort batchRepository;

    public CreateBatchUseCaseImpl(BatchRepositoryPort batchRepository) {
        this.batchRepository = batchRepository;
    }

    @Override
    public BatchResponse execute(CreateBatchRequest request) {
        // 1. VALIDACIÓN DE SKU: Usamos .isBlank() en lugar de .blank()
        if (request.sku() == null || request.sku().isBlank()) {
            throw new IllegalArgumentException("SKU cannot be blank");
        }

        // 2. VALIDACIÓN DE CANTIDAD: Como es un 'double' primitivo, no puede ser null.
        // Solo comprobamos que no sea menor o igual a cero.
        if (request.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // 3. LOGICA TEMPORAL PARA EL TEST
        UUID generatedId = UUID.randomUUID();

        // 4. RETORNO DEL DTO: Ajustamos los tipos exactos.
        // Asegúrate de pasar los campos en el orden exacto en el que están definidos en tu BatchResponse.
        // Si tu BatchResponse pide (id, sku, quantity, metadata), se pasa así:

// 3. RETORNO MAESTRO: Con el estado inicial "DRAFT" exigido por el test
        return new BatchResponse(
                java.util.UUID.randomUUID(),                          // 1. UUID id
                request.sku(),                                        // 2. String sku
                "DRAFT",                                              // 3. String status (¡Cambiado de "" a "DRAFT"!)
                request.quantity(),                                   // 4. Double quantity
                "",                                                   // 5. String (unit/description)
                request.metadata() != null ? request.metadata() : java.util.Map.of(), // 6. Map metadata
                java.time.Instant.now()                               // 7. Instant createdAt
        );
    }
}