package com.nominal.lynx.application.port.in;

import java.util.UUID;

/**
 * ChangeStatusUseCase — Puerto de entrada para cambiar estado 🔌
 *
 * Orquesta:
 * 1. Validar que la transición es válida (dominio)
 * 2. Actualizar el lote (persistencia)
 * 3. Registrar evento en Ledger (auditoría)
 */
public interface ChangeStatusUseCase {

    /**
     * Cambia el estado de un lote a uno nuevo.
     *
     * @param batchId ID del lote
     * @param newStatus Nuevo estado (como String: "ACTIVE", "DISPATCHED"...)
     * @param userId Quién hace el cambio (para auditoría)
     * @param reason Razón opcional del cambio
     * @throws com.nominal.lynx.domain.model.InvalidStateTransitionException si la transición no es válida
     */
    void execute(UUID batchId, String newStatus, UUID userId, String reason);
}