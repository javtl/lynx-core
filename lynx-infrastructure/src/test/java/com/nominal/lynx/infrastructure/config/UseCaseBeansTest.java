package com.nominal.lynx.infrastructure.config;

// 1. CORREGIDO: Importar las interfaces e implementaciones reales desde el módulo de aplicación
import com.nominal.lynx.application.port.in.CreateBatchUseCase;
import com.nominal.lynx.application.port.in.CreateBatchUseCaseImpl;
import com.nominal.lynx.domain.port.out.BatchRepository; // Asegúrate de que coincida con el nombre de tu interfaz de dominio
import com.nominal.lynx.domain.port.out.BatchRepositoryPort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class UseCaseBeansTest {

    @Test
    void createBatchUseCase_shouldReturnCreateBatchUseCaseImplementation() {
        // GIVEN
        UseCaseBeans config = new UseCaseBeans();

        // Creamos el mock con el mismo tipo de interfaz que pide el método en UseCaseBeans
        BatchRepository repository = mock(BatchRepository.class);

        // WHEN
        // Invocamos al método de configuración. Ahora compilará porque los tipos coinciden al 100%
        CreateBatchUseCase useCase = config.createBatchUseCase((BatchRepositoryPort) repository);

        // THEN
        // Verificamos que el Bean devuelto sea la instancia de la implementación real que creamos antes del break
        assertInstanceOf(CreateBatchUseCaseImpl.class, useCase);
    }
}