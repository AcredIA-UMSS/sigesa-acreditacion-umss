package com.umss.sigesa.domain.model;

import java.util.UUID;

public record NormativeIndicatorSubsanationEligibility(
        boolean canSubsanate,
        UUID openObservationId,
        String reason
) {
}
