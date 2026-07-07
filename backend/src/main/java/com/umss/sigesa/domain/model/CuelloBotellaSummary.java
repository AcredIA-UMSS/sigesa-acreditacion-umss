package com.umss.sigesa.domain.model;

@Deprecated
public record CuelloBotellaSummary(
        String indicatorId,
        String criterionCode,
        Integer daysStagnant
) {}
