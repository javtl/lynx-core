package com.nominal.lynx.domain.test;

import com.nominal.lynx.domain.exception.InconsistentStateTransitionException;
import com.nominal.lynx.domain.model.BatchRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BatchRecordTest — Test Unitario del Dominio 🧪
 *
 * Este test es la prueba definitiva de que la arquitectura es correcta:
 * - NO requiere @SpringBootTest
 * - NO levanta ningún contexto de Spring
 * - NO necesita MongoDB en memoria
 * - Se ejecuta en milisegundos
 *
 * Si puedes testear tu Dominio así, la arquitectura hexagonal está bien.
 */
@DisplayName("BatchRecord — Domain Unit Tests")
class BatchRecordTest {

    @Test
    @DisplayName("Factory method create() debe generar un lote en estado DRAFT")
    void create_shouldGenerateDraftBatch() {
        // Arrange
        var metadata = Map.<String, Object>of("salinidad", 35.2, "zona", "Marisma Norte");

        // Act
        BatchRecord batch = BatchRecord.create("SALICORNIA-001", 20.5, "kg", metadata, UUID.randomUUID());

        // Assert
        assertNotNull(batch.id(), "El ID no debe ser nulo.");
        assertEquals("SALICORNIA-001", batch.sku());
        assertEquals(BatchRecord.BatchStatus.DRAFT, batch.currentStatus(), "El estado inicial debe ser DRAFT.");
        assertEquals(20.5, batch.quantity());
        assertEquals(35.2, batch.metadata().get("salinidad"));
        assertNotNull(batch.createdAt());
    }

    @Test
    @DisplayName("transitionTo() DRAFT→ACTIVE debe devolver un nuevo Record inmutable")
    void transitionTo_draftToActive_shouldReturnNewRecord() {
        BatchRecord draft = BatchRecord.create("SKU-001", 10, "kg", null, UUID.randomUUID());

        BatchRecord active = draft.transitionTo(BatchRecord.BatchStatus.ACTIVE);

        // El Record original es INMUTABLE: draft sigue siendo DRAFT
        assertEquals(BatchRecord.BatchStatus.DRAFT, draft.currentStatus());
        // El nuevo Record tiene el estado actualizado
        assertEquals(BatchRecord.BatchStatus.ACTIVE, active.currentStatus());
        // El ID es el mismo lote, solo cambió el estado
        assertEquals(draft.id(), active.id());
    }

    @Test
    @DisplayName("transitionTo() DISPATCHED→ACTIVE debe lanzar InconsistentStateTransitionException")
    void transitionTo_invalidTransition_shouldThrow() {
        BatchRecord draft = BatchRecord.create("SKU-002", 5, "kg", null, UUID.randomUUID());
        // Forzamos un camino de estados hasta DISPATCHED
        BatchRecord dispatched = draft
                .transitionTo(BatchRecord.BatchStatus.ACTIVE)
                .transitionTo(BatchRecord.BatchStatus.COMPLETED)
                .transitionTo(BatchRecord.BatchStatus.DISPATCHED);

        // DISPATCHED es terminal: ninguna transición debería ser válida
        assertThrows(
                InconsistentStateTransitionException.class,
                () -> dispatched.transitionTo(BatchRecord.BatchStatus.ACTIVE),
                "Debería lanzar excepción al intentar transicionar desde DISPATCHED."
        );
    }

    @Test
    @DisplayName("Constructor canónico debe rechazar SKU en blanco")
    void create_blankSku_shouldThrowIllegalArgument() {
        assertThrows(
                IllegalArgumentException.class,
                () -> BatchRecord.create("  ", 10, "kg", null, UUID.randomUUID()),
                "Un SKU en blanco debe rechazarse en el Dominio."
        );
    }

    @Test
    @DisplayName("metadata null debe normalizarse a Map vacío (no NullPointerException)")
    void create_nullMetadata_shouldNormalizeToEmptyMap() {
        BatchRecord batch = BatchRecord.create("SKU-003", 5, "L", null, UUID.randomUUID());
        assertNotNull(batch.metadata());
        assertTrue(batch.metadata().isEmpty());
    }
}