package com.nominal.lynx.infrastructure.input.rest;

import com.nominal.lynx.application.dto.BatchResponse;
import com.nominal.lynx.application.port.in.CreateBatchUseCase;
import com.nominal.lynx.application.port.in.GetBatchUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean; // ◄ SOLUCIÓN: El import compatible universal
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BatchController.class)
@DisplayName("BatchController Tests")
class BatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Cambiado a @MockBean para que compile en cualquier versión de Spring Boot 3
    @MockBean
    private CreateBatchUseCase createBatchUseCase;

    @MockBean
    private GetBatchUseCase getBatchUseCase;

    @Test
    @DisplayName("POST /batches debe crear un lote con HTTP 201")
    void postBatches_shouldReturnCreated() throws Exception {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        BatchResponse response = new BatchResponse(
                batchId,
                "SALICORNIA-001",
                "DRAFT",
                20.5,
                "kg",
                Map.of(),
                Instant.now()
        );

        when(createBatchUseCase.execute(any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/batches")
                        .contentType("application/json")
                        .content("""
                        {
                          "sku": "SALICORNIA-001",
                          "quantity": 20.5,
                          "unit": "kg",
                          "userId": "%s"
                        }
                        """.formatted(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("SALICORNIA-001"))
                .andExpect(jsonPath("$.currentStatus").value("DRAFT"));
    }

    @Test
    @DisplayName("POST /batches con datos inválidos debe devolver 400")
    void postBatches_invalidData_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/batches")
                        .contentType("application/json")
                        .content("""
                        {
                          "sku": "",
                          "quantity": -10,
                          "unit": "kg",
                          "userId": null
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /batches/{id} debe devolver el lote con HTTP 200")
    void getBatch_shouldReturnBatch() throws Exception {
        // Arrange
        UUID batchId = UUID.randomUUID();
        BatchResponse response = new BatchResponse(
                batchId, "SKU-001", "DRAFT", 10.0, "kg", Map.of(), Instant.now()
        );
        when(getBatchUseCase.execute(batchId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/batches/{id}", batchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.currentStatus").value("DRAFT"));
    }

    @Test
    @DisplayName("GET /batches/{id} con ID inválido debe devolver 500")
    void getBatch_notFound_shouldThrow() throws Exception {
        UUID batchId = UUID.randomUUID();
        when(getBatchUseCase.execute(batchId))
                .thenThrow(new RuntimeException("Lote no encontrado"));

        mockMvc.perform(get("/api/v1/batches/{id}", batchId))
                .andExpect(status().isInternalServerError());
    }
}