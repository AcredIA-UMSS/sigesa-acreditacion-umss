package com.umss.sigesa.domain.model;

public record BottleneckSummary(
        String indicatorId,
        String criterionCode,
        Integer daysStagnant
) {}
