package com.nominal.lynx.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Map;
import java.util.UUID;

/**
 * CreateBatchRequest — DTO de Entrada (Input DTO)
 *
 * Representa el contrato de datos que un adaptador externo (REST Controller,
 * webhook de n8n, etc.) debe enviar para crear un lote.
 *
 * Regla: Los DTOs de Application pueden usar Jakarta Validation
 * (@NotNull, @NotBlank...) porque son un estándar Java EE, no Spring.
 * Spring Boot se encarga de invocar el validador (Hibernate Validator)
 * en la capa de infrastructure cuando llega la petición HTTP.
 *
 * Java 17 Record: inmutable por construcción, equals/hashCode/toString gratis.
 */
public record CreateBatchRequest(

        @NotBlank(message = "El SKU del producto es obligatorio.")
        String sku,

        @Positive(message = "La cantidad debe ser un valor positivo.")
        double quantity,

        @NotBlank(message = "La unidad de medida es obligatoria (ej: kg, L, ud).")
        String unit,

        /**
         * Shadow Schema: atributos dinámicos del bioproducto.
         * Ejemplos: {"salinidad": 35.2, "pH": 7.4, "zona": "Marisma Norte"}
         */
        Map<String, Object> metadata,

        @NotNull(message = "El ID del operario que crea el lote es obligatorio.")
        UUID operatorId

) {}