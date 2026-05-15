package com.nominal.lynx.domain.model;

/**
 * LedgerAction — Catálogo de acciones que se registran en el Ledger 📋
 *
 * Define QUÉ pasó con un lote. Cada evento en el Ledger tiene una acción.
 * Este enum es el diccionario de cambios válidos en el ciclo de vida del lote.
 *
 * Regla de Dominio:
 * ─────────────────
 * Las acciones son exhaustivas y excluyentes. Si una acción no está en
 * este enum, no sucedió en LYNX. Punto. Eso garantiza que la auditoría
 * es 100% confiable.
 */
public enum LedgerAction {

    /**
     * CREATE: El lote fue creado por primera vez.
     * Cuándo: Al hacer POST /batches o al ingerir texto con IA.
     * Quién: Usuario o sistema de IA.
     * Metadata típica: sku, cantidad inicial, usuario creador.
     */
    CREATE("Lote creado"),

    /**
     * STATUS_CHANGE: El estado del lote cambió (DRAFT → ACTIVE, etc).
     * Cuándo: Al hacer PATCH /batches/{id}/status.
     * Quién: Admin u Operario.
     * Metadata típica: estado anterior, estado nuevo, razón del cambio.
     */
    STATUS_CHANGE("Cambio de estado"),

    /**
     * QUANTITY_UPDATE: Se actualizó la cantidad (consumo, ajuste de inventario).
     * Cuándo: Cuando se gasta parte del lote o se rectifica.
     * Quién: Operario de almacén o Admin.
     * Metadata típica: cantidad anterior, cantidad nueva, motivo.
     */
    QUANTITY_UPDATE("Actualización de cantidad"),

    /**
     * QUALITY_CHECK: Se realizó un control de calidad (pH, salinidad, etc).
     * Cuándo: Durante el proceso de laboratorio.
     * Quién: Técnico de calidad.
     * Metadata típica: parámetro medido, valor, rango aceptable, resultado (OK/FAIL).
     */
    QUALITY_CHECK("Control de calidad"),

    /**
     * DISPATCH: El lote fue enviado (sale de las instalaciones).
     * Cuándo: Al hacer PATCH /batches/{id}/status → DISPATCHED.
     * Quién: Admin de logística.
     * Metadata típica: destino, número de seguimiento, transportista.
     */
    DISPATCH("Envío del lote");

    private final String description;

    LedgerAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}