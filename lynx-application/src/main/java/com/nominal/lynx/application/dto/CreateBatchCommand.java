package com.nominal.lynx.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Map;
import java.util.UUID;

/**
 * Application command representing client intent to create a new batch.
 */
public record CreateBatchCommand(
        @NotBlank String sku,
        @Positive double quantity,
        @NotBlank String unit,
        Map<String, Object> metadata,
        @NotNull UUID createdBy
) {
}
