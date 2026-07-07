package com.umss.sigesa.domain.model;

@Deprecated
public record FaseAvanceSummary(
        Integer phaseId,
        String name,
        Double percentage,
        String status
) {}
