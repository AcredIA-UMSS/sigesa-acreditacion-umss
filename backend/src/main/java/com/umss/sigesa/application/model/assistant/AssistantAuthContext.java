package com.umss.sigesa.application.model.assistant;

import java.util.List;
import java.util.UUID;

public record AssistantAuthContext(
        UUID userId,
        String role,
        List<UUID> programScope
) {
}
