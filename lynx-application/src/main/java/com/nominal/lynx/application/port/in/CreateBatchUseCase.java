package com.nominal.lynx.application.port.in;

import com.nominal.lynx.application.dto.CreateBatchRequest;
import com.nominal.lynx.application.dto.BatchResponse;

/**
 * CreateBatchUseCase — Puerto de entrada (Input Port)
 *
 * Define el CONTRATO del caso de uso.
 * El controller depende de esto (interface), nunca de la implementación.
 */
public interface CreateBatchUseCase {
    BatchResponse execute(CreateBatchRequest request);
}