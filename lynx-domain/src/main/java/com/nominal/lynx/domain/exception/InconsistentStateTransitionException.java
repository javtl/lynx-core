package com.nominal.lynx.domain.exception;

import com.nominal.lynx.domain.model.BatchRecord;

/**
 * Raised when a requested state change violates the batch lifecycle rules.
 */
public class InconsistentStateTransitionException extends RuntimeException {

    public InconsistentStateTransitionException(
            BatchRecord.BatchStatus from,
            BatchRecord.BatchStatus to
    ) {
        super("Invalid status transition from " + from + " to " + to);
    }
}
