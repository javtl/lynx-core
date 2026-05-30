package com.nominal.lynx.application.usecase;

import com.nominal.lynx.application.port.in.ChangeStatusUseCase;
import com.nominal.lynx.domain.model.BatchRecord;
import com.nominal.lynx.domain.model.InvalidStateTransitionException;
import com.nominal.lynx.domain.port.out.BatchRepositoryPort;
import com.nominal.lynx.domain.port.out.LedgerRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ChangeStatusUseCase Tests")
class ChangeStatusUseCaseTest {

    @Test
    @DisplayName("execute() debe cambiar estado de DRAFT a ACTIVE")
    void execute_draftToActive_shouldSucceed() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        BatchRecord draft = BatchRecord.create("SKU", 10, "kg", null, userId);

        BatchRepositoryPort mockBatchRepo = mock(BatchRepositoryPort.class);
        LedgerRepositoryPort mockLedgerRepo = mock(LedgerRepositoryPort.class);

        when(mockBatchRepo.findById(batchId)).thenReturn(Optional.of(draft));

        ChangeStatusUseCase useCase = new ChangeStatusUseCaseImpl(mockBatchRepo, mockLedgerRepo);

        // Act
        useCase.execute(batchId, "ACTIVE", userId, "Revisado");

        // Assert
        verify(mockBatchRepo, times(1)).save(any());  // Se guardó el cambio
        verify(mockLedgerRepo, times(1)).append(any()); // Se registró el evento
    }

    @Test
    @DisplayName("execute() debe lanzar InvalidStateTransitionException si la transición es inválida")
    void execute_invalidTransition_shouldThrow() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Crear un lote en estado DISPATCHED (terminal)
        BatchRecord draft = BatchRecord.create("SKU", 10, "kg", null, userId);
        BatchRecord active = draft.transitionTo(BatchRecord.BatchStatus.ACTIVE);
        BatchRecord completed = active.transitionTo(BatchRecord.BatchStatus.COMPLETED);
        BatchRecord dispatched = completed.transitionTo(BatchRecord.BatchStatus.DISPATCHED);

        BatchRepositoryPort mockBatchRepo = mock(BatchRepositoryPort.class);
        LedgerRepositoryPort mockLedgerRepo = mock(LedgerRepositoryPort.class);

        when(mockBatchRepo.findById(batchId)).thenReturn(Optional.of(dispatched));

        ChangeStatusUseCase useCase = new ChangeStatusUseCaseImpl(mockBatchRepo, mockLedgerRepo);

        // Act & Assert
        assertThrows(InvalidStateTransitionException.class, () ->
                useCase.execute(batchId, "ACTIVE", userId, "Intento inválido")
        );
    }

    @Test
    @DisplayName("execute() debe lanzar RuntimeException si el estado es inválido")
    void execute_invalidStatusString_shouldThrow() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        BatchRecord draft = BatchRecord.create("SKU", 10, "kg", null, userId);

        BatchRepositoryPort mockBatchRepo = mock(BatchRepositoryPort.class);
        LedgerRepositoryPort mockLedgerRepo = mock(LedgerRepositoryPort.class);

        when(mockBatchRepo.findById(batchId)).thenReturn(Optional.of(draft));

        ChangeStatusUseCase useCase = new ChangeStatusUseCaseImpl(mockBatchRepo, mockLedgerRepo);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                useCase.execute(batchId, "ESTADO_INEXISTENTE", userId, "Intento")
        );
    }
}