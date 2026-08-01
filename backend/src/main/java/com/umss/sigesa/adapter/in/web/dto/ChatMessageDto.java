package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageDto(
        @NotBlank String role,
        @NotBlank String content
) {
}
