package com.nominal.lynx.infrastructure.config;

import com.nominal.lynx.application.usecase.CreateBatchService;
import com.nominal.lynx.domain.port.in.CreateBatchUseCase;
import com.nominal.lynx.domain.port.out.BatchRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class UseCaseBeansTest {

    @Test
    void createBatchUseCase_shouldReturnCreateBatchServiceImplementation() {
        UseCaseBeans config = new UseCaseBeans();
        BatchRepository repository = mock(BatchRepository.class);

        CreateBatchUseCase useCase = config.createBatchUseCase(repository);

        assertInstanceOf(CreateBatchService.class, useCase);
    }
}
