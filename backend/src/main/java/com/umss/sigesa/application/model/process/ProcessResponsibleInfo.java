package com.umss.sigesa.application.model.process;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProcessResponsibleInfo(
        UUID userId,
        String fullName,
        String email,
        LocalDateTime assignedAt
) {
}
