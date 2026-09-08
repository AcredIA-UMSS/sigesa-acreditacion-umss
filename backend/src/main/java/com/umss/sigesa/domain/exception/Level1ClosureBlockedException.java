package com.umss.sigesa.domain.exception;

import com.umss.sigesa.domain.model.PendingIndicator;

import java.util.List;

public class Level1ClosureBlockedException extends RuntimeException {

    private final List<PendingIndicator> pendingIndicators;

    public Level1ClosureBlockedException(String message, List<PendingIndicator> pendingIndicators) {
        super(message);
        this.pendingIndicators = List.copyOf(pendingIndicators);
    }

    public List<PendingIndicator> getPendingIndicators() {
        return pendingIndicators;
    }
}
