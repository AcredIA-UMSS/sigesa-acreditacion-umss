package com.umss.sigesa.domain.model;

import java.util.List;

public record StageGateResult(
        boolean pass,
        List<String> failedRules,
        String summary) {

    public static StageGateResult pass(String summary) {
        return new StageGateResult(true, List.of(), summary);
    }

    public static StageGateResult block(List<String> failedRules, String summary) {
        return new StageGateResult(false, List.copyOf(failedRules), summary);
    }
}
