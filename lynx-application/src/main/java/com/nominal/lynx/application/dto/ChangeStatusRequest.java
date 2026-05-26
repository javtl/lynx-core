package com.nominal.lynx.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * ChangeStatusRequest — DTO para cambiar el estado de un lote 📨
 *
 * Ejemplo JSON desde el cliente:
 * { "newStatus": "ACTIVE", "reason": "Revisado por QA" }
 */
public record ChangeStatusRequest(

        @NotBlank(message = "El nuevo estado es obligatorio.")
        String newStatus,

        String reason  // Opcional

) {}