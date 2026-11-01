package com.umss.sigesa.domain.model;

import java.util.Locale;
import java.util.Optional;

public enum FodaQuadrant {
    STRENGTH,
    WEAKNESS,
    OPPORTUNITY,
    THREAT;

    public static Optional<FodaQuadrant> from(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(FodaQuadrant.valueOf(raw.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
