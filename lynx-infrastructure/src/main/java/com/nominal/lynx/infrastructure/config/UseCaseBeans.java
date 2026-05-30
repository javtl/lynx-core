package com.nominal.lynx.infrastructure.config;

import com.nominal.lynx.application.port.in.CreateBatchUseCase;
import com.nominal.lynx.application.port.in.GetBatchUseCase;
import com.nominal.lynx.application.port.in.ChangeStatusUseCase;
import com.nominal.lynx.application.port.in.CreateBatchUseCaseImpl;
import com.nominal.lynx.application.port.in.GetBatchUseCaseImpl;
import com.nominal.lynx.application.usecase.ChangeStatusUseCaseImpl;
import com.nominal.lynx.domain.port.out.BatchRepositoryPort;
import com.nominal.lynx.domain.port.out.LedgerRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * UseCaseBeans — Configuración de Casos de Uso
 *
 * ─────────────────────────────────────────────────────────────────────
 * FLUJO DE DEPENDENCIAS (Dependency Injection Manual):
 * ─────────────────────────────────────────────────────────────────────
 *
 * Spring instancia BatchMongoAdapter (tiene @Component en infrastructure)
 * │
 * ▼ implementa
 * BatchRepository (interface en domain)
 * │
 * ▼ se inyecta en
 * CreateBatchUseCaseImpl (clase en application, SIN @Service)
 * │
 * ▼ registrado como @Bean por
 * UseCaseBeans (esta clase, en infrastructure)
 * │
 * ▼ consumido por
 * BatchController (tiene @RestController en infrastructure)
 *
 * ─────────────────────────────────────────────────────────────────────
 * BENEFICIO:
 * - domain: 0 anotaciones Spring → Clean, testeable, portable.
 * - application: 0 anotaciones Spring → Pure Java, testeable con JUnit puro.
 * - infrastructure: Toda la "magia" de Spring contenida aquí.
 * ─────────────────────────────────────────────────────────────────────
 */
@Configuration
public class UseCaseBeans {

    @Bean
    public CreateBatchUseCase createBatchUseCase(BatchRepositoryPort batchRepository) {
        return new CreateBatchUseCaseImpl(batchRepository);
    }

    @Bean
    public GetBatchUseCase getBatchUseCase(BatchRepositoryPort batchRepository) {
        return new GetBatchUseCaseImpl(batchRepository);
    }

    @Bean
    public ChangeStatusUseCase changeStatusUseCase(
            BatchRepositoryPort batchRepository,
            LedgerRepositoryPort ledgerRepository) {
        return new ChangeStatusUseCaseImpl(batchRepository, ledgerRepository);
    }
}