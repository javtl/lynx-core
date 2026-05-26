package com.nominal.lynx.application.port.in;

import com.nominal.lynx.application.dto.BatchResponse;

import java.util.UUID;

/**
 * GetBatchUseCase — Puerto de entrada para consultar un lote 🔌
 *
 * Define el CONTRATO del caso de uso de lectura.
 */
public interface GetBatchUseCase {
    BatchResponse execute(UUID batchId);
}