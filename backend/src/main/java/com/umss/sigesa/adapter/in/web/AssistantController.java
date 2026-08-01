package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.AssistantStatusResponse;
import com.umss.sigesa.adapter.in.web.dto.ChatMessageDto;
import com.umss.sigesa.adapter.in.web.dto.SendChatMessageRequest;
import com.umss.sigesa.adapter.in.web.dto.SendChatMessageResponse;
import com.umss.sigesa.application.port.in.SendChatMessageUseCase;
import com.umss.sigesa.config.AssistantProperties;
import com.umss.sigesa.domain.model.ChatMessage;
import com.umss.sigesa.domain.model.ChatRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/assistant")
@Tag(name = "Assistant", description = "Asistente virtual SIGESA (proxy Open WebUI)")
public class AssistantController {

    private final SendChatMessageUseCase sendChatMessageUseCase;
    private final AssistantProperties assistantProperties;

    public AssistantController(SendChatMessageUseCase sendChatMessageUseCase,
                               AssistantProperties assistantProperties) {
        this.sendChatMessageUseCase = sendChatMessageUseCase;
        this.assistantProperties = assistantProperties;
    }

    @GetMapping("/status")
    @Operation(summary = "Estado del asistente", description = "Indica si el asistente está habilitado y qué modelo usa.")
    public AssistantStatusResponse getStatus() {
        return new AssistantStatusResponse(
                assistantProperties.isEnabled(),
                assistantProperties.getModel()
        );
    }

    @PostMapping("/chat")
    @Operation(summary = "Enviar mensaje al asistente", description = "Proxy hacia Open WebUI (API compatible OpenAI).")
    public ResponseEntity<SendChatMessageResponse> chat(@Valid @RequestBody SendChatMessageRequest request) {
        List<ChatMessage> history = request.history() == null
                ? Collections.emptyList()
                : request.history().stream()
                        .map(dto -> new ChatMessage(parseRole(dto.role()), dto.content()))
                        .toList();

        String reply = sendChatMessageUseCase.send(request.message(), history);
        return ResponseEntity.ok(new SendChatMessageResponse(reply));
    }

    private static ChatRole parseRole(String role) {
        return switch (role.toLowerCase()) {
            case "assistant" -> ChatRole.ASSISTANT;
            case "system" -> ChatRole.SYSTEM;
            default -> ChatRole.USER;
        };
    }
}
