package com.nominal.lynx.domain.model;

import com.nominal.lynx.domain.exception.InconsistentStateTransitionException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable aggregate root for a production batch.
 * <p>
 * This type lives in the domain layer and contains only business rules:
 * invariants at creation time and valid status transitions.
 */
public record BatchRecord(
        UUID id,
        String sku,
        double quantity,
        String unit,
        Map<String, Object> metadata,
        BatchStatus currentStatus,
        UUID createdBy,
        Instant createdAt
) {
    /**
     * Canonical constructor enforcing domain invariants.
     */
    public BatchRecord {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("unit must not be blank");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        currentStatus = currentStatus == null ? BatchStatus.DRAFT : currentStatus;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    /**
     * Factory method for creating a new draft batch with generated identifier and timestamp.
     */
    public static BatchRecord create(
            String sku,
            double quantity,
            String unit,
            Map<String, Object> metadata,
            UUID createdBy
    ) {
        return new BatchRecord(
                UUID.randomUUID(),
                sku,
                quantity,
                unit,
                metadata,
                BatchStatus.DRAFT,
                createdBy,
                Instant.now()
        );
    }

    /**
     * Returns a new immutable instance with the next status if transition is allowed.
     *
     * @throws InconsistentStateTransitionException when transition is invalid
     */
    public BatchRecord transitionTo(BatchStatus nextStatus) {
        if (!currentStatus.canTransitionTo(nextStatus)) {
            throw new InconsistentStateTransitionException(currentStatus, nextStatus);
        }
        return new BatchRecord(id, sku, quantity, unit, metadata, nextStatus, createdBy, createdAt);
    }

    /**
     * Domain lifecycle for a batch.
     */
    public enum BatchStatus {
        DRAFT,
        ACTIVE,
        COMPLETED,
        DISPATCHED,
        DISCARDED;

        /**
         * Validates if transition to the given status is allowed by domain rules.
         */
        public boolean canTransitionTo(BatchStatus next) {
            return switch (this) {
                case DRAFT -> next == ACTIVE || next == DISCARDED;
                case ACTIVE -> next == COMPLETED || next == DISCARDED;
                case COMPLETED -> next == DISPATCHED || next == DISCARDED;
                case DISPATCHED, DISCARDED -> false;
            };
        }
    }
}
