package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.AssistantDemoScenarioResponse;
import com.umss.sigesa.adapter.in.web.dto.AssistantStatusResponse;
import com.umss.sigesa.adapter.in.web.dto.ChatMessageDto;
import com.umss.sigesa.adapter.in.web.dto.SendChatMessageRequest;
import com.umss.sigesa.adapter.in.web.dto.SendChatMessageResponse;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantChatResult;
import com.umss.sigesa.application.port.in.SendChatMessageUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.assistant.AssistantCapabilitiesCatalog;
import com.umss.sigesa.config.AssistantProperties;
import com.umss.sigesa.domain.exception.AssistantUnavailableException;
import com.umss.sigesa.domain.model.ChatMessage;
import com.umss.sigesa.domain.model.ChatRole;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assistant")
@Tag(name = "Assistant", description = "Asistente virtual SIGESA (tool calling + motor directo)")
public class AssistantController {

    private static final List<AssistantDemoScenarioResponse> DEMO_SCENARIOS = List.of(
            new AssistantDemoScenarioResponse(
                    1,
                    "Controlado (catálogo)",
                    "Lista las fases de Ingeniería de Sistemas CEUB",
                    "KEYWORD"),
            new AssistantDemoScenarioResponse(
                    2,
                    "Sinónimo (LLM elige tool)",
                    "¿Qué etapas tiene el proceso activo de Ingeniería de Sistemas CEUB?",
                    "LLM"),
            new AssistantDemoScenarioResponse(
                    3,
                    "Fuera de alcance",
                    "¿Cuál es el presupuesto de la universidad para 2027?",
                    "OUT_OF_SCOPE"),
            new AssistantDemoScenarioResponse(
                    4,
                    "Modelo apagado",
                    "Lista las fases de Ingeniería de Sistemas CEUB (SIGESA_ASSISTANT_LLM_ENABLED=false)",
                    "KEYWORD")
    );

    private final SendChatMessageUseCase sendChatMessageUseCase;
    private final AssistantProperties assistantProperties;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public AssistantController(SendChatMessageUseCase sendChatMessageUseCase,
                               AssistantProperties assistantProperties,
                               UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.sendChatMessageUseCase = sendChatMessageUseCase;
        this.assistantProperties = assistantProperties;
        this.assignmentRepository = assignmentRepository;
    }

    @GetMapping("/status")
    @Operation(summary = "Estado del asistente", description = "Indica flags, modelo, capacidades y escenarios demo.")
    public AssistantStatusResponse getStatus() {
        AssistantAuthContext authContext = buildAuthContext();
        return new AssistantStatusResponse(
                assistantProperties.isEnabled(),
                assistantProperties.isLlmEnabled(),
                assistantProperties.getModel(),
                AssistantCapabilitiesCatalog.capabilitiesForRole(authContext.role()),
                DEMO_SCENARIOS);
    }

    @PostMapping("/chat")
    @Operation(summary = "Enviar mensaje al asistente", description = "Ejecuta tools vía código; LLM solo elige tool si aplica.")
    public ResponseEntity<SendChatMessageResponse> chat(@Valid @RequestBody SendChatMessageRequest request) {
        if (!assistantProperties.isEnabled()) {
            throw new AssistantUnavailableException("El asistente está deshabilitado.");
        }

        List<ChatMessage> history = request.history() == null
                ? Collections.emptyList()
                : request.history().stream()
                        .map(dto -> new ChatMessage(parseRole(dto.role()), dto.content()))
                        .toList();

        AssistantAuthContext authContext = buildAuthContext();
        AssistantChatResult result = sendChatMessageUseCase.send(request.message(), history, authContext);
        return ResponseEntity.ok(new SendChatMessageResponse(
                result.reply(),
                result.toolId(),
                result.sourceTables(),
                result.path().name(),
                result.llmInvoked()));
    }

    private AssistantAuthContext buildAuthContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = extractUserId(auth);
        String role = extractRole(auth);
        List<UUID> programScope = extractProgramScope(userId);
        return new AssistantAuthContext(userId, role, programScope);
    }

    private static UUID extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Usuario no autenticado.");
        }
        if (auth.getPrincipal() instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(auth.getPrincipal().toString());
    }

    private static String extractRole(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            throw new IllegalStateException("Usuario sin rol asignado.");
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Usuario sin rol asignado."));
    }

    private List<UUID> extractProgramScope(UUID userId) {
        return assignmentRepository.findActiveByUserId(userId).stream()
                .map(UserProgramAssignment::getProgramId)
                .toList();
    }

    private static ChatRole parseRole(String role) {
        return switch (role.toLowerCase()) {
            case "assistant" -> ChatRole.ASSISTANT;
            case "system" -> ChatRole.SYSTEM;
            case "tool" -> ChatRole.TOOL;
            default -> ChatRole.USER;
        };
    }
}
