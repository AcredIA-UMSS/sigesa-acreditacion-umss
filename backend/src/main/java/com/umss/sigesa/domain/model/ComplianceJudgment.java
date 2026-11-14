package com.umss.sigesa.domain.model;

import java.util.Locale;
import java.util.Optional;

public enum ComplianceJudgment {
    COMPLIES,
    PARTIAL,
    DOES_NOT_COMPLY;

    public static Optional<ComplianceJudgment> from(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(ComplianceJudgment.valueOf(raw.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
