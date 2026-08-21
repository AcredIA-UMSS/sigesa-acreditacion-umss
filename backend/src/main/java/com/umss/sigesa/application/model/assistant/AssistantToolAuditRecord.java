package com.umss.sigesa.application.model.assistant;

import java.util.UUID;

public record AssistantToolAuditRecord(
        UUID userId,
        String role,
        String agentId,
        String toolId,
        String sideEffect,
        boolean success,
        String outcomeCode
) {
}
