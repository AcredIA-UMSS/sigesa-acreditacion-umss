package com.umss.sigesa.domain.exception;

import com.umss.sigesa.domain.model.PendingSubphase;

import java.util.List;

public class PhaseClosureBlockedException extends RuntimeException {

    private final List<PendingSubphase> pendingSubphases;

    public PhaseClosureBlockedException(String message, List<PendingSubphase> pendingSubphases) {
        super(message);
        this.pendingSubphases = List.copyOf(pendingSubphases);
    }

    public List<PendingSubphase> getPendingSubphases() {
        return pendingSubphases;
    }
}
