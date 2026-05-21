package com.nominal.lynx.infrastructure.output.persistence;

import com.nominal.lynx.domain.model.BatchRecord;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * BatchDocument — Entidad de MongoDB para los Lotes 🍃
 *
 * Esta clase es el "traductor" entre el dominio puro y la base de datos.
 * El dominio usa BatchRecord (immutable, sin anotaciones).
 * MongoDB usa BatchDocument (mutable, con @Document, @Id, @Indexed).
 *
 * Responsabilidad:
 * ───────────────
 * Mapear los datos de BatchRecord a un formato que MongoDB entienda,
 * y viceversa.
 *
 * IMPORTANTE:
 * ──────────
 * Esta clase NO se usa en el Dominio. Solo existe en infrastructure.
 * Los Use Cases trabajan con BatchRecord; el Adapter convierte entre
 * BatchRecord ↔ BatchDocument.
 *
 * Decisión de diseño: Clase mutable vs Record
 * ───────────────────────────────────────────
 * Spring Data necesita setters para hidratar los objetos desde MongoDB.
 * Por eso BatchDocument es una clase tradicional, no un Record.
 * Esto NO rompe la inmutabilidad del Dominio, porque el Dominio nunca
 * ve esta clase.
 */
@Document(collection = "batches")
public class BatchDocument {

    @Id
    private UUID id;

    @Indexed
    @Field("sku")
    private String sku;

    @Indexed
    @Field("current_status")
    private String currentStatus;  // Guardamos como String para flexibilidad

    @Field("quantity")
    private Double quantity;

    @Field("unit")
    private String unit;

    @Field("metadata")
    private Map<String, Object> metadata;

    @Field("created_at")
    private Instant createdAt;

    @Field("created_by")
    private UUID createdBy;

    // =========================================================================
    // CONSTRUCTORES
    // =========================================================================

    /**
     * Constructor vacío obligatorio para Spring Data.
     * No lo uses manualmente; es para que MongoDB pueda hidratar objetos.
     */
    public BatchDocument() {
    }

    /**
     * Constructor completo para crear documentos desde el Adapter.
     */
    public BatchDocument(UUID id, String sku, String currentStatus, Double quantity,
                         String unit, Map<String, Object> metadata, Instant createdAt, UUID createdBy) {
        this.id = id;
        this.sku = sku;
        this.currentStatus = currentStatus;
        this.quantity = quantity;
        this.unit = unit;
        this.metadata = metadata;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    // =========================================================================
    // FACTORY METHOD — De Dominio a Documento
    // =========================================================================

    /**
     * Convierte un BatchRecord (dominio) a BatchDocument (infraestructura).
     * Este método vive aquí para mantener la responsabilidad del mapeo
     * dentro de la capa de infrastructure.
     */
    public static BatchDocument fromDomain(BatchRecord batch) {
        return new BatchDocument(
                batch.id(),
                batch.sku(),
                batch.currentStatus().name(),  // Enum → String
                batch.quantity(),
                batch.unit(),
                batch.metadata(),
                batch.createdAt(),
                batch.createdByUserId()
        );
    }

    // =========================================================================
    // MAPPER METHOD — De Documento a Dominio
    // =========================================================================

    /**
     * Convierte este BatchDocument (infraestructura) a BatchRecord (dominio).
     * El dominio recupera su inmutabilidad al salir de la base de datos.
     */
    public BatchRecord toDomain() {
        return new BatchRecord(
                        this.id,
                        this.sku,
                this.quantity,  // String → Enum
                this.unit,
                this.metadata,
                BatchRecord.BatchStatus.valueOf(this.currentStatus),
                this.createdBy,
                this.createdAt
                );
    }

    // =========================================================================
    // GETTERS Y SETTERS (Obligatorios para Spring Data)
    // =========================================================================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }
}