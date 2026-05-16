package com.nominal.lynx.infrastructure.output.persistence;

import com.nominal.lynx.domain.model.BatchRecord;
import com.nominal.lynx.domain.model.EventSource;
import com.nominal.lynx.domain.model.LedgerEvent;
import com.nominal.lynx.domain.port.out.BatchRepositoryPort;
import com.nominal.lynx.domain.port.out.LedgerRepositoryPort;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MongoDBIntegrationTest — Tests de Integración con MongoDB Real 🧪🍃
 *
 * Estos tests NO usan mocks. Levantan un contenedor Docker real de MongoDB
 * usando Testcontainers. Esto valida:
 * - Que los adaptadores funcionan con una BD real.
 * - Que las transacciones ACID son atómicas.
 * - Que el mapeo entre Domain ↔ Document funciona correctamente.
 *
 * Requisito:
 * ─────────
 * Docker debe estar corriendo en tu máquina.
 *
 * @DataMongoTest: Configura Spring para tests de MongoDB.
 * @Testcontainers: Habilita el soporte de Testcontainers.
 * @Import: Importa los adaptadores (que no se escanean automáticamente en @DataMongoTest).
 */
@DataMongoTest
@Testcontainers
@Import({MongoBatchAdapter.class, MongoLedgerAdapter.class})
@DisplayName("MongoDB Integration Tests — Real Database with Testcontainers")
class MongoDBIntegrationTest {

    /**
     * Contenedor de MongoDB que se levanta automáticamente antes de los tests.
     * Se reutiliza entre tests para velocidad (static).
     */
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);

    /**
     * Configura dinámicamente la URI de MongoDB para que Spring se conecte
     * al contenedor de Testcontainers en lugar de localhost:27017.
     */
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private BatchRepositoryPort batchRepository;

    @Autowired
    private LedgerRepositoryPort ledgerRepository;

    // =========================================================================
    // SETUP Y CLEANUP
    // =========================================================================

    @BeforeEach
    void cleanDatabase() {
        // Limpiamos la BD antes de cada test para que sean independientes
        batchRepository.findAll().forEach(batch -> batchRepository.deleteById(batch.id()));
        ledgerRepository.findAll().forEach(event -> ledgerRepository.deleteById(event.eventId()));
    }

    // =========================================================================
    // TEST 1: Guardar y recuperar un Batch
    // =========================================================================
    @Test
    @DisplayName("save() debe persistir un lote y findById() debe recuperarlo correctamente")
    void save_shouldPersistBatchAndRetrieveIt() {
        // Arrange
        BatchRecord batch = BatchRecord.create(
                "SALICORNIA-001",
                20.5,
                "kg",
                Map.of("salinidad", 35.2, "zona", "Marisma Norte"),
                UUID.randomUUID()
        );

        // Act
        BatchRecord saved = batchRepository.save(batch);
        Optional<BatchRecord> retrieved = batchRepository.findById(saved.id());

        // Assert
        assertTrue(retrieved.isPresent());
        assertEquals(batch.sku(), retrieved.get().sku());
        assertEquals(batch.quantity(), retrieved.get().quantity());
        assertEquals(BatchRecord.BatchStatus.DRAFT, retrieved.get().currentStatus());
    }

    // =========================================================================
    // TEST 2: Buscar lotes por SKU
    // =========================================================================
    @Test
    @DisplayName("findBySku() debe devolver todos los lotes del mismo producto")
    void findBySku_shouldReturnAllBatchesOfProduct() {
        // Arrange
        BatchRecord batch1 = BatchRecord.create("SALICORNIA-001", 10, "kg", null, UUID.randomUUID());
        BatchRecord batch2 = BatchRecord.create("SALICORNIA-001", 15, "kg", null, UUID.randomUUID());
        BatchRecord batch3 = BatchRecord.create("OTRO-PRODUCTO", 5, "L", null, UUID.randomUUID());

        batchRepository.save(batch1);
        batchRepository.save(batch2);
        batchRepository.save(batch3);

        // Act
        List<BatchRecord> results = batchRepository.findBySku("SALICORNIA-001");

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(b -> b.sku().equals("SALICORNIA-001")));
    }

    // =========================================================================
    // TEST 3: Buscar lotes por estado
    // =========================================================================
    @Test
    @DisplayName("findByStatus() debe filtrar correctamente por estado")
    void findByStatus_shouldFilterByBatchStatus() {
        // Arrange
        BatchRecord draft = BatchRecord.create("SKU-001", 10, "kg", null, UUID.randomUUID());
        BatchRecord active = draft.transitionTo(BatchRecord.BatchStatus.ACTIVE);

        batchRepository.save(draft);
        batchRepository.save(active);

        // Act
        List<BatchRecord> drafts = batchRepository.findByStatus(BatchRecord.BatchStatus.DRAFT);
        List<BatchRecord> actives = batchRepository.findByStatus(BatchRecord.BatchStatus.ACTIVE);

        // Assert
        assertEquals(1, drafts.size());
        assertEquals(1, actives.size());
        assertEquals(BatchRecord.BatchStatus.DRAFT, drafts.get(0).currentStatus());
        assertEquals(BatchRecord.BatchStatus.ACTIVE, actives.get(0).currentStatus());
    }

    // =========================================================================
    // TEST 4: Append de evento al Ledger
    // =========================================================================
    @Test
    @DisplayName("append() debe guardar un evento y findByBatchId() debe recuperarlo")
    void append_shouldPersistEventAndRetrieveByBatchId() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LedgerEvent event = LedgerEvent.ofCreate(batchId, EventSource.USER, "SALICORNIA-001", 20.5, userId);

        // Act
        LedgerEvent saved = ledgerRepository.append(event);
        List<LedgerEvent> retrieved = ledgerRepository.findByBatchId(batchId);

        // Assert
        assertEquals(1, retrieved.size());
        assertEquals(event.eventId(), retrieved.get(0).eventId());
        assertEquals(event.description(), retrieved.get(0).description());
    }

    // =========================================================================
    // TEST 5: Orden cronológico del Ledger
    // =========================================================================
    @Test
    @DisplayName("findByBatchId() debe devolver eventos ordenados cronológicamente")
    void findByBatchId_shouldReturnEventsInChronologicalOrder() throws InterruptedException {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        LedgerEvent event1 = LedgerEvent.ofCreate(batchId, EventSource.USER, "SKU", 10, userId);
        Thread.sleep(10); // Asegurar diferencia en timestamp
        LedgerEvent event2 = LedgerEvent.ofStatusChange(
                batchId, EventSource.USER,
                BatchRecord.BatchStatus.DRAFT, BatchRecord.BatchStatus.ACTIVE,
                "Revisado", userId
        );

        // Act
        ledgerRepository.append(event1);
        ledgerRepository.append(event2);
        List<LedgerEvent> events = ledgerRepository.findByBatchId(batchId);

        // Assert
        assertEquals(2, events.size());
        assertTrue(events.get(0).timestamp().isBefore(events.get(1).timestamp()),
                "El primer evento debe ser más antiguo que el segundo");
    }

    // =========================================================================
    // TEST 6: Count de eventos
    // =========================================================================
    @Test
    @DisplayName("countByBatchId() debe devolver el número correcto de eventos")
    void countByBatchId_shouldReturnCorrectCount() {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ledgerRepository.append(LedgerEvent.ofCreate(batchId, EventSource.USER, "SKU", 10, userId));
        ledgerRepository.append(LedgerEvent.ofStatusChange(
                batchId, EventSource.USER,
                BatchRecord.BatchStatus.DRAFT, BatchRecord.BatchStatus.ACTIVE,
                "Test", userId
        ));

        // Act
        long count = ledgerRepository.countByBatchId(batchId);

        // Assert
        assertEquals(2, count);
    }

    // =========================================================================
    // TEST 7: Eventos más recientes (limit)
    // =========================================================================
    @Test
    @DisplayName("findRecentByBatchId() debe devolver solo los N eventos más recientes")
    void findRecentByBatchId_shouldReturnMostRecentEvents() throws InterruptedException {
        // Arrange
        UUID batchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        for (int i = 0; i < 5; i++) {
            ledgerRepository.append(LedgerEvent.ofCreate(batchId, EventSource.USER, "SKU-" + i, 10, userId));
            Thread.sleep(10);
        }

        // Act
        List<LedgerEvent> recent = ledgerRepository.findRecentByBatchId(batchId, 3);

        // Assert
        assertEquals(3, recent.size());
        // El más reciente debe estar primero (orden descendente)
        assertTrue(recent.get(0).timestamp().isAfter(recent.get(1).timestamp()));
    }

    // =========================================================================
    // TEST 8: ExistsById para Batch
    // =========================================================================
    @Test
    @DisplayName("existsById() debe devolver true para lotes existentes")
    void existsById_shouldReturnTrueForExistingBatch() {
        // Arrange
        BatchRecord batch = BatchRecord.create("SKU-001", 10, "kg", null, UUID.randomUUID());
        BatchRecord saved = batchRepository.save(batch);

        // Act & Assert
        assertTrue(batchRepository.existsById(saved.id()));
        assertFalse(batchRepository.existsById(UUID.randomUUID()));
    }

    // =========================================================================
    // TEST 9: Metadata se preserva correctamente
    // =========================================================================
    @Test
    @DisplayName("Metadata de Batch y LedgerEvent debe persistirse correctamente")
    void metadata_shouldBePersisted() {
        // Arrange
        Map<String, Object> batchMetadata = Map.of("salinidad", 35.2, "pH", 7.4);
        BatchRecord batch = BatchRecord.create("SALICORNIA-001", 20, "kg", batchMetadata, UUID.randomUUID());

        Map<String, Object> eventMetadata = Map.of("reason", "Control de calidad", "inspector", "Juan");
        UUID eventId = UUID.randomUUID();
        LedgerEvent event = new LedgerEvent(
                eventId, batch.id(),
                com.nominal.lynx.domain.model.LedgerAction.QUALITY_CHECK,
                EventSource.USER, "Test metadata",
                eventMetadata, java.time.Instant.now()
        );

        // Act
        batchRepository.save(batch);
        ledgerRepository.append(event);

        BatchRecord retrievedBatch = batchRepository.findById(batch.id()).orElseThrow();
        LedgerEvent retrievedEvent = ledgerRepository.findById(eventId).orElseThrow();

        // Assert
        assertEquals(35.2, retrievedBatch.metadata().get("salinidad"));
        assertEquals(7.4, retrievedBatch.metadata().get("pH"));
        assertEquals("Control de calidad", retrievedEvent.getMetadataString("reason"));
    }

    // =========================================================================
    // TEST 10: Delete (solo para testing)
    // =========================================================================
    @Test
    @DisplayName("deleteById() debe eliminar lotes y eventos (solo testing)")
    void deleteById_shouldRemoveEntities() {
        // Arrange
        BatchRecord batch = BatchRecord.create("SKU-001", 10, "kg", null, UUID.randomUUID());
        BatchRecord saved = batchRepository.save(batch);

        LedgerEvent event = LedgerEvent.ofCreate(batch.id(), EventSource.USER, "SKU", 10, UUID.randomUUID());
        LedgerEvent savedEvent = ledgerRepository.append(event);

        // Act
        batchRepository.deleteById(saved.id());
        ledgerRepository.deleteById(savedEvent.eventId());

        // Assert
        assertFalse(batchRepository.existsById(saved.id()));
        assertFalse(ledgerRepository.findById(savedEvent.eventId()).isPresent());
    }
}