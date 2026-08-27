package com.umss.sigesa.domain.model;

import java.util.UUID;

public record SubphaseRejectResult(
        UUID subphaseId,
        UUID observationId,
        SubphaseTransitionResult transition
) {
}
