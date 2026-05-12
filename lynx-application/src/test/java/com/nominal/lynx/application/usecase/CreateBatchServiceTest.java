package com.nominal.lynx.application.usecase;

import com.nominal.lynx.application.dto.CreateBatchCommand;
import com.nominal.lynx.domain.model.BatchRecord;
import com.nominal.lynx.domain.port.out.BatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateBatchServiceTest {

    @Mock
    private BatchRepository batchRepository;

    @InjectMocks
    private CreateBatchService createBatchService;

    @Test
    void create_shouldBuildBatchAndSaveUsingRepository() {
        UUID creatorId = UUID.randomUUID();
        when(batchRepository.save(any(BatchRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BatchRecord created = createBatchService.create("SKU-APP-001", 11.5, "kg", Map.of("farm", "north"), creatorId);

        ArgumentCaptor<BatchRecord> captor = ArgumentCaptor.forClass(BatchRecord.class);
        verify(batchRepository).save(captor.capture());

        BatchRecord saved = captor.getValue();
        assertEquals("SKU-APP-001", saved.sku());
        assertEquals(11.5, saved.quantity());
        assertEquals("kg", saved.unit());
        assertEquals(BatchRecord.BatchStatus.DRAFT, saved.currentStatus());
        assertEquals(creatorId, saved.createdBy());
        assertEquals(saved, created);
    }

    @Test
    void create_withCommand_shouldDelegateToPrimaryMethod() {
        UUID creatorId = UUID.randomUUID();
        CreateBatchCommand command = new CreateBatchCommand("SKU-APP-002", 3.0, "L", Map.of(), creatorId);
        when(batchRepository.save(any(BatchRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BatchRecord created = createBatchService.create(command);

        assertEquals("SKU-APP-002", created.sku());
        verify(batchRepository).save(any(BatchRecord.class));
    }

    @Test
    void create_withInvalidData_shouldBubbleDomainValidationError() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> createBatchService.create(" ", 1.0, "kg", Map.of(), UUID.randomUUID())
        );

        assertTrue(error.getMessage().contains("sku"));
    }
}
