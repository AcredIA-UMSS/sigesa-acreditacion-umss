package com.umss.sigesa.application.model.evidence;

import com.umss.sigesa.domain.model.IndicatorState;

import java.util.UUID;

public record UploadableIndicator(
        UUID indicatorId,
        String code,
        String title,
        UUID criterionId,
        String criterionCode,
        String criterionTitle,
        IndicatorState currentState
) {
}
