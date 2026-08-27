package com.umss.sigesa.domain.model;

import java.util.UUID;

public record SubphaseSubsanationEligibility(
        boolean canSubsanate,
        UUID openObservationId,
        String reason) {
}
