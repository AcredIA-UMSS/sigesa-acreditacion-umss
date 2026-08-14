package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.AssistantDemoScenarioResponse;
import com.umss.sigesa.adapter.in.web.dto.AssistantStatusResponse;
import com.umss.sigesa.adapter.in.web.dto.ChatMessageDto;
import com.umss.sigesa.adapter.in.web.dto.SendChatMessageRequest;
import com.umss.sigesa.adapter.in.web.dto.SendChatMessageResponse;
import com.umss.sigesa.application.model.assistant.AssistantAgentProfile;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantChatContext;
import com.umss.sigesa.application.model.assistant.AssistantChatResult;
import com.umss.sigesa.application.port.in.SendChatMessageUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.assistant.AssistantCapabilitiesCatalog;
import com.umss.sigesa.application.service.assistant.AssistantChatContextFactory;
import com.umss.sigesa.config.AssistantProperties;
import com.umss.sigesa.domain.exception.AssistantAgentAccessDeniedException;
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
import org.springframework.web.bind.annotation.RequestParam;
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

    private static final List<AssistantDemoScenarioResponse> PHASES_COPILOT_SAMPLES = List.of(
            new AssistantDemoScenarioResponse(
                    1,
                    "Listar fases (contexto)",
                    "Lista las fases de este proceso",
                    "KEYWORD"),
            new AssistantDemoScenarioResponse(
                    2,
                    "Estructura con subfases",
                    "Muestra la estructura completa con subfases y enlaces",
                    "KEYWORD"),
            new AssistantDemoScenarioResponse(
                    3,
                    "Sinónimo",
                    "¿Qué etapas tiene este proceso?",
                    "LLM"),
            new AssistantDemoScenarioResponse(
                    4,
                    "Edición subfase (JD/TD)",
                    "Agrega una subfase «Evidencia docente» con enlace HTTPS en la Fase 1",
                    "LLM")
    );

    private static final List<AssistantDemoScenarioResponse> USERS_COPILOT_SAMPLES = List.of(
            new AssistantDemoScenarioResponse(
                    1,
                    "Listar usuarios",
                    "Lista los usuarios registrados",
                    "KEYWORD"),
            new AssistantDemoScenarioResponse(
                    2,
                    "Filtrar por rol",
                    "¿Qué usuarios CC están activos?",
                    "LLM"),
            new AssistantDemoScenarioResponse(
                    3,
                    "Detalle",
                    "Muéstrame el detalle de cc@umss.edu.bo",
                    "LLM"),
            new AssistantDemoScenarioResponse(
                    4,
                    "Desactivar (confirmación)",
                    "Desactiva al usuario cc@umss.edu.bo",
                    "KEYWORD")
    );

    private static final List<AssistantDemoScenarioResponse> EVIDENCE_COPILOT_SAMPLES = List.of(
            new AssistantDemoScenarioResponse(
                    1,
                    "Pendientes de revisión",
                    "¿Qué evidencias de mi carrera están pendientes de revisión?",
                    "KEYWORD"),
            new AssistantDemoScenarioResponse(
                    2,
                    "Detalle de evidencia",
                    "Muéstrame el detalle de la evidencia del indicador Plan de estudios vigente",
                    "LLM"),
            new AssistantDemoScenarioResponse(
                    3,
                    "Completitud",
                    "¿La evidencia del indicador Plan de estudios vigente está completa?",
                    "LLM")
    );

    private final SendChatMessageUseCase sendChatMessageUseCase;
    private final AssistantProperties assistantProperties;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final AssistantChatContextFactory chatContextFactory;

    public AssistantController(SendChatMessageUseCase sendChatMessageUseCase,
                               AssistantProperties assistantProperties,
                               UserProgramAssignmentRepositoryPort assignmentRepository,
                               AssistantChatContextFactory chatContextFactory) {
        this.sendChatMessageUseCase = sendChatMessageUseCase;
        this.assistantProperties = assistantProperties;
        this.assignmentRepository = assignmentRepository;
        this.chatContextFactory = chatContextFactory;
    }

    @GetMapping("/status")
    @Operation(summary = "Estado del asistente", description = "Indica flags, modelo, capacidades y escenarios demo.")
    public AssistantStatusResponse getStatus(
            @RequestParam(name = "agent", required = false) String agent) {
        AssistantAuthContext authContext = buildAuthContext();
        AssistantAgentProfile agentProfile = AssistantAgentProfile.fromAgentId(agent);
        assertUsersAgentAccess(agentProfile, authContext.role());
        assertEvidenceAgentAccess(agentProfile, authContext.role());
        List<AssistantDemoScenarioResponse> scenarios = switch (agentProfile) {
            case PHASES -> PHASES_COPILOT_SAMPLES;
            case USERS -> USERS_COPILOT_SAMPLES;
            case EVIDENCE -> EVIDENCE_COPILOT_SAMPLES;
            default -> DEMO_SCENARIOS;
        };
        String agentId = switch (agentProfile) {
            case PHASES -> "phases";
            case USERS -> "users";
            case EVIDENCE -> "evidence";
            default -> "general";
        };
        return new AssistantStatusResponse(
                assistantProperties.isEnabled(),
                assistantProperties.isLlmEnabled(),
                assistantProperties.getModel(),
                AssistantCapabilitiesCatalog.capabilitiesForRoleAndAgent(
                        authContext.role(), agentProfile),
                scenarios,
                agentId);
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
        AssistantChatContext chatContext = resolveChatContext(request, authContext);
        AssistantChatResult result = sendChatMessageUseCase.send(
                request.message(), history, authContext, chatContext);
        return ResponseEntity.ok(new SendChatMessageResponse(
                result.reply(),
                result.toolId(),
                result.sourceTables(),
                result.path().name(),
                result.llmInvoked()));
    }

    private AssistantChatContext resolveChatContext(SendChatMessageRequest request,
                                                      AssistantAuthContext authContext) {
        if (request.context() == null) {
            return AssistantChatContext.general();
        }
        AssistantAgentProfile profile = AssistantAgentProfile.fromAgentId(request.context().agent());
        assertUsersAgentAccess(profile, authContext.role());
        assertEvidenceAgentAccess(profile, authContext.role());
        return chatContextFactory.resolve(
                request.context().agent(),
                request.context().processId(),
                request.context().userId(),
                request.context().programId(),
                authContext);
    }

    private static void assertUsersAgentAccess(AssistantAgentProfile profile, String role) {
        if (profile == AssistantAgentProfile.USERS
                && (role == null || !"JD".equalsIgnoreCase(role.trim()))) {
            throw new AssistantAgentAccessDeniedException("users");
        }
    }

    private static void assertEvidenceAgentAccess(AssistantAgentProfile profile, String role) {
        if (profile != AssistantAgentProfile.EVIDENCE) {
            return;
        }
        if (role == null) {
            throw new AssistantAgentAccessDeniedException("evidence", "JD, TD o CC");
        }
        String normalized = role.trim().toUpperCase();
        if (!"JD".equals(normalized) && !"TD".equals(normalized) && !"CC".equals(normalized)) {
            throw new AssistantAgentAccessDeniedException("evidence", "JD, TD o CC");
        }
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
