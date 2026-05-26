package com.nominal.lynx.application.usecase;

import com.nominal.lynx.application.dto.CreateBatchRequest;
import com.nominal.lynx.application.dto.BatchResponse;
import com.nominal.lynx.application.port.in.CreateBatchUseCase;
import com.nominal.lynx.application.port.in.CreateBatchUseCaseImpl;
import com.nominal.lynx.domain.port.out.BatchRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("CreateBatchUseCase Tests")
class CreateBatchUseCaseTest {

    @Test
    @DisplayName("execute() debe crear un lote y devolverlo en DTO")
    void execute_shouldCreateBatchAndReturnResponse() {
        // Arrange
        BatchRepositoryPort mockRepo = mock(BatchRepositoryPort.class);
        CreateBatchUseCase useCase = new CreateBatchUseCaseImpl(mockRepo);

        UUID userId = UUID.randomUUID();
        CreateBatchRequest request = new CreateBatchRequest(
                "SALICORNIA-001",
                20.5,
                "kg",
                Map.of("salinidad", 35.2),
                userId
        );

        // Act
        BatchResponse response = useCase.execute(request);

        // Assert
        assertNotNull(response.id());
        assertEquals("SALICORNIA-001", response.sku());
        assertEquals(20.5, response.quantity());
        assertEquals("DRAFT", response.currentStatus());
        assertEquals(35.2, response.metadata().get("salinidad"));

        verify(mockRepo, times(1)).save(any());
    }

    @Test
    @DisplayName("execute() debe validar SKU no en blanco")
    void execute_blankSku_shouldThrow() {
        BatchRepositoryPort mockRepo = mock(BatchRepositoryPort.class);
        CreateBatchUseCase useCase = new CreateBatchUseCaseImpl(mockRepo);

        CreateBatchRequest request = new CreateBatchRequest(
                "",  // SKU en blanco
                20.5,
                "kg",
                null,
                UUID.randomUUID()
        );

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));
    }

    @Test
    @DisplayName("execute() debe validar cantidad positiva")
    void execute_negativeQuantity_shouldThrow() {
        BatchRepositoryPort mockRepo = mock(BatchRepositoryPort.class);
        CreateBatchUseCase useCase = new CreateBatchUseCaseImpl(mockRepo);

        CreateBatchRequest request = new CreateBatchRequest(
                "SKU-001",
                -10.0,  // Cantidad negativa
                "kg",
                null,
                UUID.randomUUID()
        );

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));
    }

    @Test
    @DisplayName("execute() debe mapear metadata correctamente")
    void execute_shouldMapMetadataCorrectly() {
        BatchRepositoryPort mockRepo = mock(BatchRepositoryPort.class);
        CreateBatchUseCase useCase = new CreateBatchUseCaseImpl(mockRepo);

        Map<String, Object> metadata = Map.of(
                "salinidad", 35.2,
                "pH", 7.4,
                "zona", "Marisma Norte"
        );

        CreateBatchRequest request = new CreateBatchRequest(
                "SKU-001",
                20.5,
                "kg",
                metadata,
                UUID.randomUUID()
        );

        BatchResponse response = useCase.execute(request);

        assertEquals(35.2, response.metadata().get("salinidad"));
        assertEquals(7.4, response.metadata().get("pH"));
        assertEquals("Marisma Norte", response.metadata().get("zona"));
    }
}