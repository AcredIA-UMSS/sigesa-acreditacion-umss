package com.umss.sigesa.domain.model;

public record PhaseProgressSummary(
        Integer phaseId,
        String name,
        Double percentage,
        String status
) {}
