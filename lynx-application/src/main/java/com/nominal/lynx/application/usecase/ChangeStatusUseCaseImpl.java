package com.nominal.lynx.application.usecase;

import com.nominal.lynx.application.port.in.ChangeStatusUseCase;
import com.nominal.lynx.domain.model.BatchRecord;
import com.nominal.lynx.domain.model.LedgerEvent;
import com.nominal.lynx.domain.model.EventSource;
import com.nominal.lynx.domain.port.out.BatchRepositoryPort;
import com.nominal.lynx.domain.port.out.LedgerRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * ChangeStatusUseCaseImpl — Cambio de estado con transacción ACID 🔧
 *
 * @Transactional: Si algo falla, TODO se revierte (atomicidad).
 *
 * Flujo:
 * 1. Recuperar lote actual
 * 2. Validar transición (el dominio lanza excepción si es inválida)
 * 3. Crear nuevo lote con nuevo estado (Records son inmutables)
 * 4. Guardar lote actualizado
 * 5. Registrar evento en Ledger
 * Si algo falla → rollback automático
 */
public class ChangeStatusUseCaseImpl implements ChangeStatusUseCase {

    private final BatchRepositoryPort batchRepository;
    private final LedgerRepositoryPort ledgerRepository;

    public ChangeStatusUseCaseImpl(BatchRepositoryPort batchRepository,
                                   LedgerRepositoryPort ledgerRepository) {
        this.batchRepository = batchRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @Override
    @Transactional
    public void execute(UUID batchId, String newStatus, UUID userId, String reason) {
        // 1. Recuperar lote actual
        BatchRecord batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Lote no encontrado: " + batchId));

        // 2. Convertir String → Enum (con manejo de error si es inválido)
        BatchRecord.BatchStatus targetStatus;
        try {
            targetStatus = BatchRecord.BatchStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido: " + newStatus);
        }

        // 3. Validar transición (el dominio lanza InvalidStateTransitionException si no es válida)
        BatchRecord updatedBatch = batch.transitionTo(targetStatus);

        // 4. Persistir lote actualizado
        batchRepository.save(updatedBatch);

        // 5. Registrar evento en Ledger (append-only)
        LedgerEvent event = LedgerEvent.ofStatusChange(
                batchId,
                EventSource.USER,
                batch.currentStatus(),
                targetStatus,
                reason != null ? reason : "Sin especificar",
                userId
        );
        ledgerRepository.append(event);
    }
}