package com.umss.sigesa.domain.model;

import java.util.UUID;

public record SubphaseApproveResult(
        UUID subphaseId,
        SubphaseTransitionResult transition
) {
}
