package com.nominal.lynx.application.dto;

import com.nominal.lynx.domain.model.BatchRecord;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * BatchResponse — DTO de salida que devolvemos al cliente 📤
 *
 * Se construye desde un BatchRecord (dominio) usando fromDomain().
 * Así el dominio NO se filtra al cliente: solo exponemos lo que queremos.
 */
public record BatchResponse(
        UUID id,
        String sku,
        String currentStatus,
        Double quantity,
        String unit,
        Map<String, Object> metadata,
        Instant createdAt
) {

    /**
     * Mapper: Dominio (BatchRecord) → DTO (BatchResponse)
     * El controller llama esto para convertir antes de serializar a JSON.
     */
    public static BatchResponse fromDomain(BatchRecord batch) {
        return new BatchResponse(
                batch.id(),
                batch.sku(),
                batch.currentStatus().name(),  // Enum → String
                batch.quantity(),
                batch.unit(),
                batch.metadata(),
                batch.createdAt()
        );
    }
}