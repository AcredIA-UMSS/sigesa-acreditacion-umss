package com.umss.sigesa.domain.model;

import java.util.UUID;

public record SubphaseTransitionResult(
        UUID subphaseId,
        SubphaseState previousState,
        SubphaseState newState
) {
}
