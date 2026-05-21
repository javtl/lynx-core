package com.nominal.lynx.domain.test;


import com.nominal.lynx.domain.model.BatchRecord;
import com.nominal.lynx.domain.model.LedgerEvent;
import com.nominal.lynx.domain.model.LedgerAction;
import com.nominal.lynx.domain.model.EventSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LedgerEventTest — Tests unitarios del Ledger 🧪
 *
 * Valida que los eventos del Ledger son:
 * - Immutables (una vez creados, no cambian)
 * - Válidos (no aceptan datos inconsistentes)
 * - Trazables (contienen toda la info necesaria para auditar)
 *
 * Estos tests NO requieren Spring, MongoDB ni Testcontainers.
 * Son puro JUnit: rápidos, deterministas, aislados.
 */
@DisplayName("LedgerEvent — Immutable Audit Trail Tests")
class LedgerEventTest {

    // =========================================================================
    // TEST 1: Crear un evento CREATE válido
    // =========================================================================
    @Test
    @DisplayName("ofCreate() debe generar un evento válido con todos los campos requeridos")
    void ofCreate_shouldGenerateValidCreateEvent() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Act
        LedgerEvent event = LedgerEvent.ofCreate(
                batchId,
                EventSource.USER,
                "SALICORNIA-001",
                20.5,
                userId
        );

        // Assert
        assertNotNull(event.eventId(), "El eventId debe ser único.");
        assertEquals(batchId, event.batchId());
        assertEquals(LedgerAction.CREATE, event.action());
        assertEquals(EventSource.USER, event.source());
        assertNotNull(event.timestamp());
        assertTrue(event.timestamp().isBefore(Instant.now().plusSeconds(1)), "El timestamp debe ser ahora.");
        assertEquals("SALICORNIA-001", event.getMetadataString("sku"));
        assertEquals(20.5, event.getMetadataDouble("quantity"));
    }

    // =========================================================================
    // TEST 2: Crear un evento STATUS_CHANGE válido
    // =========================================================================
    @Test
    @DisplayName("ofStatusChange() debe registrar correctamente una transición de estado")
    void ofStatusChange_shouldRecordStateTransition() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Act
        LedgerEvent event = LedgerEvent.ofStatusChange(
                batchId,
                EventSource.USER,
                BatchRecord.BatchStatus.DRAFT,
                BatchRecord.BatchStatus.ACTIVE,
                "Revisado por QA",
                userId
        );

        // Assert
        assertEquals(LedgerAction.STATUS_CHANGE, event.action());
        assertEquals("DRAFT", event.getMetadataString("previousStatus"));
        assertEquals("ACTIVE", event.getMetadataString("newStatus"));
        assertEquals("Revisado por QA", event.getMetadataString("reason"));
        assertTrue(event.isStatusChange());
    }

    // =========================================================================
    // TEST 3: El Ledger no permite timestamps en el futuro
    // =========================================================================
    @Test
    @DisplayName("Constructor debe rechazar timestamp en el futuro (anomalía de reloj)")
    void constructor_futureTimestamp_shouldThrow() {
        // Arrange
        Instant futureTimestamp = Instant.now().plusSeconds(3600);

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new LedgerEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        LedgerAction.CREATE,
                        EventSource.USER,
                        "Evento con timestamp futuro",
                        Map.of(),
                        futureTimestamp
                ),
                "No se debe permitir timestamp en el futuro."
        );
    }

    // =========================================================================
    // TEST 4: Inmutabilidad: metadata no puede ser modificada
    // =========================================================================
    @Test
    @DisplayName("Metadata debe ser inmutable (no se puede modificar después de creación)")
    void metadata_shouldBeImmutable() {
        // Arrange
        Map<String, Object> mutableMetadata = new java.util.HashMap<>();
        mutableMetadata.put("sku", "SALICORNIA-001");

        // Act
        LedgerEvent event = new LedgerEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LedgerAction.CREATE,
                EventSource.USER,
                "Test inmutabilidad",
                mutableMetadata,
                Instant.now()
        );

        // Assert: intentar modificar el map original no afecta el evento
        mutableMetadata.put("sku", "MODIFICADO");
        assertEquals("SALICORNIA-001", event.getMetadataString("sku"),
                "El evento debe tener su propia copia inmutable de metadata.");
    }

    // =========================================================================
    // TEST 5: Métodos de origen del evento (EventSource)
    // =========================================================================
    @Test
    @DisplayName("isUserOriginated(), isIoTOriginated(), isSystemOriginated() deben ser correctos")
    void eventSourceChecks_shouldIdentifyOriginCorrectly() {
        // Act & Assert para USER
        LedgerEvent userEvent = new LedgerEvent(
                UUID.randomUUID(), UUID.randomUUID(), LedgerAction.CREATE,
                EventSource.USER, "Test", Map.of(), Instant.now()
        );
        assertTrue(userEvent.isUserOriginated());
        assertFalse(userEvent.isIoTOriginated());
        assertFalse(userEvent.isSystemOriginated());

        // Act & Assert para IOT_SENSOR
        LedgerEvent iotEvent = new LedgerEvent(
                UUID.randomUUID(), UUID.randomUUID(), LedgerAction.QUALITY_CHECK,
                EventSource.IOT_SENSOR, "Test", Map.of(), Instant.now()
        );
        assertFalse(iotEvent.isUserOriginated());
        assertTrue(iotEvent.isIoTOriginated());
        assertFalse(iotEvent.isSystemOriginated());

        // Act & Assert para SYSTEM
        LedgerEvent systemEvent = new LedgerEvent(
                UUID.randomUUID(), UUID.randomUUID(), LedgerAction.STATUS_CHANGE,
                EventSource.SYSTEM, "Test", Map.of(), Instant.now()
        );
        assertFalse(systemEvent.isUserOriginated());
        assertFalse(systemEvent.isIoTOriginated());
        assertTrue(systemEvent.isSystemOriginated());
    }

    // =========================================================================
    // TEST 6: Crear un evento QUALITY_CHECK
    // =========================================================================
    @Test
    @DisplayName("ofQualityCheck() debe registrar parámetros de control de calidad")
    void ofQualityCheck_shouldRecordQualityParameters() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Act
        LedgerEvent event = LedgerEvent.ofQualityCheck(
                batchId,
                EventSource.IOT_SENSOR,
                "pH",
                7.2,
                "OK",
                userId
        );

        // Assert
        assertEquals(LedgerAction.QUALITY_CHECK, event.action());
        assertEquals("pH", event.getMetadataString("parameter"));
        assertEquals(7.2, event.getMetadataDouble("value"));
        assertEquals("OK", event.getMetadataString("status"));
    }

    // =========================================================================
    // TEST 7: Crear un evento DISPATCH
    // =========================================================================
    @Test
    @DisplayName("ofDispatch() debe registrar información de envío")
    void ofDispatch_shouldRecordShippingInfo() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Act
        LedgerEvent event = LedgerEvent.ofDispatch(
                batchId,
                EventSource.USER,
                "Almacén de Huelva",
                "Transporcar SL",
                userId
        );

        // Assert
        assertEquals(LedgerAction.DISPATCH, event.action());
        assertEquals("Almacén de Huelva", event.getMetadataString("destination"));
        assertEquals("Transporcar SL", event.getMetadataString("carrier"));
    }

    // =========================================================================
    // TEST 8: Validación: description no puede estar en blanco
    // =========================================================================
    @Test
    @DisplayName("Constructor debe rechazar description en blanco")
    void constructor_blankDescription_shouldThrow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LedgerEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        LedgerAction.CREATE,
                        EventSource.USER,
                        "   ", // solo espacios
                        Map.of(),
                        Instant.now()
                ),
                "Description no puede estar en blanco."
        );
    }

    // =========================================================================
    // TEST 9: Métodos de acceso a metadata (getMetadataValue, getMetadataDouble, etc.)
    // =========================================================================
    @Test
    @DisplayName("Métodos getMetadata*() deben extraer valores de forma segura")
    void metadataAccessors_shouldExtractValuesSafely() {
        // Arrange
        Map<String, Object> metadata = Map.of(
                "sku", "SALICORNIA-001",
                "quantity", 20.5,
                "zone", "Marisma Norte"
        );
        LedgerEvent event = new LedgerEvent(
                UUID.randomUUID(), UUID.randomUUID(), LedgerAction.CREATE,
                EventSource.USER, "Test", metadata, Instant.now()
        );

        // Act & Assert
        assertEquals("SALICORNIA-001", event.getMetadataString("sku"));
        assertEquals(20.5, event.getMetadataDouble("quantity"));
        assertNull(event.getMetadataValue("nonexistent"), "Valor inexistente debe devolver null.");
        assertNull(event.getMetadataString("nonexistent"), "String inexistente debe devolver null.");
        assertNull(event.getMetadataDouble("nonexistent"), "Double inexistente debe devolver null.");
    }
}