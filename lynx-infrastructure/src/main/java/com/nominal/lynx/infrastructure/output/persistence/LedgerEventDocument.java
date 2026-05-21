package com.nominal.lynx.infrastructure.output.persistence;

import com.nominal.lynx.domain.model.LedgerEvent;
import com.nominal.lynx.domain.model.LedgerAction;
import com.nominal.lynx.domain.model.EventSource;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * LedgerEventDocument — Entidad MongoDB para el Ledger 📖🍃
 *
 * Representa un evento inmutable en el Ledger (Libro Mayor).
 * Esta es la clase que MongoDB entiende para persistir LedgerEvent.
 *
 * Índice compuesto:
 * ────────────────
 * batch_id + timestamp (descendente) para consultas de auditoría rápidas:
 * "Dame todos los eventos del lote #xyz, del más reciente al más antiguo"
 */
@Document(collection = "ledger_events")
@CompoundIndex(name = "batch_timestamp_idx", def = "{'batch_id': 1, 'timestamp': -1}")
public class LedgerEventDocument {

    @Id
    private UUID eventId;

    @Indexed
    @Field("batch_id")
    private UUID batchId;

    @Field("action")
    private String action;

    @Field("source")
    private String source;

    @Field("description")
    private String description;

    @Field("metadata")
    private Map<String, Object> metadata;

    @Indexed
    @Field("timestamp")
    private Instant timestamp;

    // =========================================================================
    // CONSTRUCTORES
    // =========================================================================

    public LedgerEventDocument() {
    }

    public LedgerEventDocument(UUID eventId, UUID batchId, String action, String source,
                               String description, Map<String, Object> metadata, Instant timestamp) {
        this.eventId = eventId;
        this.batchId = batchId;
        this.action = action;
        this.source = source;
        this.description = description;
        this.metadata = metadata;
        this.timestamp = timestamp;
    }

    // =========================================================================
    // MAPPERS
    // =========================================================================

    public static LedgerEventDocument fromDomain(LedgerEvent event) {
        return new LedgerEventDocument(
                event.eventId(),
                event.batchId(),
                event.action().name(),
                event.source().name(),
                event.description(),
                event.metadata(),
                event.timestamp()
        );
    }

    public LedgerEvent toDomain() {
        return new LedgerEvent(
                this.eventId,
                this.batchId,
                LedgerAction.valueOf(this.action),
                EventSource.valueOf(this.source),
                this.description,
                this.metadata,
                this.timestamp
        );
    }

    // =========================================================================
    // GETTERS Y SETTERS
    // =========================================================================

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}