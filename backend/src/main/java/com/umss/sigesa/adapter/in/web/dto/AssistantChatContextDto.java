package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record AssistantChatContextDto(
        @Pattern(regexp = "phases|users|general", message = "agent debe ser 'phases', 'users' o 'general'")
        String agent,
        UUID processId,
        String careerName,
        String careerCode,
        String templateType,
        UUID userId,
        UUID programId
) {
}
