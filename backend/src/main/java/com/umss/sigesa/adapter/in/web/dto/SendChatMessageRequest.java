package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record SendChatMessageRequest(
        @NotBlank String message,
        @Valid List<ChatMessageDto> history
) {
}
