package com.nominal.lynx.domain.model;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(BatchRecord.BatchStatus from, BatchRecord.BatchStatus to) {
        super("Transición de estado inválida: de " + from + " a " + to);
    }
}
