package com.umss.sigesa.domain.model;

import java.util.UUID;

public record PendingSubphase(
        UUID subphaseId,
        String name,
        SubphaseState status,
        Integer order
) {
}
