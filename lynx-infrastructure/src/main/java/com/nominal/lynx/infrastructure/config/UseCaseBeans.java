package com.nominal.lynx.infrastructure.config;

import com.nominal.lynx.application.usecase.CreateBatchService;
import com.nominal.lynx.domain.port.in.CreateBatchUseCase;
import com.nominal.lynx.domain.port.out.BatchRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Infrastructure wiring for application use cases.
 */
@Configuration
public class UseCaseBeans {

    /**
     * Wires input port to its application service implementation.
     */
    @Bean
    public CreateBatchUseCase createBatchUseCase(BatchRepository batchRepository) {
        return new CreateBatchService(batchRepository);
    }
}
