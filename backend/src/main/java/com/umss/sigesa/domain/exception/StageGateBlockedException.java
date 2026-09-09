package com.umss.sigesa.domain.exception;

import com.umss.sigesa.domain.model.StageGateResult;

import java.util.List;

public class StageGateBlockedException extends RuntimeException {

    private final List<String> failedRules;

    public StageGateBlockedException(String message, StageGateResult gateResult) {
        super(message);
        this.failedRules = gateResult.failedRules();
    }

    public List<String> getFailedRules() {
        return failedRules;
    }
}
