package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.model.assistant.AssistantAgentProfile;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantChatContext;
import com.umss.sigesa.application.model.assistant.AssistantChatResult;
import com.umss.sigesa.application.model.assistant.AssistantResolutionPath;
import com.umss.sigesa.application.model.assistant.AssistantToolDefinition;
import com.umss.sigesa.application.model.assistant.AssistantToolInvocation;
import com.umss.sigesa.application.model.assistant.AssistantToolStep;
import com.umss.sigesa.application.model.assistant.ChatCompletionRequest;
import com.umss.sigesa.application.model.assistant.ChatCompletionResult;
import com.umss.sigesa.application.model.assistant.ToolCall;
import com.umss.sigesa.application.model.assistant.ToolExecutionResult;
import com.umss.sigesa.application.port.in.SendChatMessageUseCase;
import com.umss.sigesa.application.port.out.ChatCompletionPort;
import com.umss.sigesa.domain.model.ChatMessage;
import com.umss.sigesa.domain.model.ChatRole;
import com.umss.sigesa.domain.model.ChatToolCall;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SendChatMessageService implements SendChatMessageUseCase {

    private static final String TOOL_SELECTION_PROMPT_SUFFIX = """

            MODO SELECCIÓN DE TOOL (obligatorio):
            - Tu única tarea es elegir la tool correcta o indicar que ya no necesitas más tools.
            - NUNCA redactes la respuesta final al usuario ni inventes datos.
            - Invoca como máximo UNA tool por turno. Si necesitas más datos, invoca otra tool en el siguiente turno tras ver el resultado JSON.
            - Cuando ya tengas toda la información necesaria para responder, responde con content vacío y SIN tool_calls.
            - Sinónimos permitidos: «etapas» = fases del proceso; «carreras» = programas; «proceso en curso» = proceso activo.
            - Si la pregunta es sobre presupuesto, finanzas, clima, noticias u otro tema ajeno a acreditación, responde con content vacío y SIN tool_calls.
            - Si ninguna tool aplica (dato fuera del sistema), responde con content vacío y SIN tool_calls.
            """;

    private static final String PHASES_AGENT_PROMPT_SUFFIX = """

            CONTEXTO COPILOTO DE FASES (obligatorio):
            - El usuario está viendo un proceso de acreditación concreto en pantalla.
            - Usa SIEMPRE careerQuery=%s y templateType=%s en los argumentos de las tools (no pidas la carrera).
            - Solo tools de fases: list_process_phases, list_process_structure (lectura) y manage_process_phase / manage_process_subphase (escritura con confirmación, solo JD/TD).
            - search_normative_docs para preguntas sobre normativa CEUB/ARCU-SUR vinculada a subfases.
            - No invoques tools de usuarios, programas ni otros procesos.
            - Fases del proceso (usa phaseOrder o phaseId REAL; NUNCA inventes UUIDs como UUID_FASE_1):
            %s
            - Para crear subfase: action=CREATE, phaseOrder=N (o phaseName), name=..., referenceUrl=https://..., confirmed=false primero; confirmed=true solo tras confirmación del usuario.
            - No envíes el campo order al crear subfases: el sistema informa el último orden existente y asigna el siguiente disponible en la vista previa.
            - «Fase N» identifica la fase contenedora (phaseOrder), NO el orden de la subfase.
            - Encadenamiento típico: list_process_structure → search_normative_docs cuando pidan estructura y normativa de subfases.
            """;

    private static final String USERS_AGENT_PROMPT_SUFFIX = """

            CONTEXTO COPILOTO DE USUARIOS (obligatorio):
            - El usuario es Jefatura DUEA [JD] en /admin/users.
            - Solo tools de usuarios: list_users, get_user_detail, create_user, manage_user_status, manage_user_assignment.
            - No invoques tools de fases ni procesos.
            - create_user / manage_*: confirmed=false primero; confirmed=true solo tras confirmación explícita.
            - Correos solo @umss.edu.bo. CC/EE requieren programId (o programQuery).
            - Alta deja la cuenta INACTIVE hasta el primer acceso.
            - Encadenamiento típico: list_users → get_user_detail cuando pidan listado filtrado y detalle de un usuario concreto.
            """;

    private static final String EVIDENCE_AGENT_PROMPT_SUFFIX = """

            CONTEXTO COPILOTO DE CONTROL DOCUMENTAL (obligatorio):
            - Solo lectura: list_pending_evidences, get_evidence_detail, check_evidence_completeness.
            - No apruebes ni rechaces indicadores (fuera de MVP).
            - list_pending_evidences: indicadores en estado SUBIDO; programId opcional.
            - get_evidence_detail / check_evidence_completeness: requieren indicatorId UUID real.
            - PBAC: JD/TD alcance institucional; CC solo su programScope JWT.
            - No invoques tools de usuarios ni de fases.
            - Para preguntas normativas (CEUB, ARCU-SUR, criterios, subfases): search_normative_docs.
            - Encadenamiento típico: list_pending_evidences → search_normative_docs cuando pidan pendientes y normativa relacionada.
            """;

    private static final String RAG_TOOL_HINT = """

            RAG NORMATIVO (obligatorio cuando aplique):
            - search_normative_docs: busca fragmentos de acreditación universitaria indexados (CEUB, ARCU-SUR, DUEA).
            - Usa esta tool cuando la pregunta sea sobre normativa, criterios, requisitos documentales o enlaces de subfases.
            - Pasa query con las palabras clave del usuario; templateType opcional (CEUB o ARCU-SUR).
            """;

    private static final String MAX_ITERATIONS_SUFFIX = """

            Límite de encadenamiento: máximo %d tools por mensaje. Si aún faltan datos, el sistema devolverá resultados parciales.
            """;

    private final ChatCompletionPort chatCompletionPort;
    private final AssistantToolRegistry toolRegistry;
    private final AssistantToolExecutor toolExecutor;
    private final AssistantKeywordRouter keywordRouter;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;
    private final boolean llmEnabled;
    private final int maxToolIterations;
    private final AssistantNormativeRagService normativeRagService;

    public SendChatMessageService(ChatCompletionPort chatCompletionPort,
                                  AssistantToolRegistry toolRegistry,
                                  AssistantToolExecutor toolExecutor,
                                  AssistantKeywordRouter keywordRouter,
                                  ObjectMapper objectMapper,
                                  String systemPrompt,
                                  boolean llmEnabled,
                                  int maxToolIterations,
                                  AssistantNormativeRagService normativeRagService) {
        this.chatCompletionPort = chatCompletionPort;
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
        this.keywordRouter = keywordRouter;
        this.objectMapper = objectMapper;
        this.systemPrompt = systemPrompt;
        this.llmEnabled = llmEnabled;
        this.maxToolIterations = Math.max(1, maxToolIterations);
        this.normativeRagService = normativeRagService;
    }

    @Override
    public AssistantChatResult send(String userMessage,
                                    List<ChatMessage> history,
                                    AssistantAuthContext authContext,
                                    AssistantChatContext chatContext) {
        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío.");
        }
        if (authContext == null) {
            throw new IllegalArgumentException("El contexto de autenticación es obligatorio.");
        }

        AssistantChatContext effectiveContext = chatContext == null
                ? AssistantChatContext.general()
                : chatContext;
        AssistantAgentProfile agentProfile = effectiveContext.agentProfile();

        Optional<AssistantToolInvocation> keywordMatch =
                keywordRouter.resolve(userMessage, history, authContext, effectiveContext);
        if (keywordMatch.isPresent()) {
            return executeSingleTool(
                    keywordMatch.get(),
                    AssistantResolutionPath.KEYWORD,
                    false,
                    authContext,
                    effectiveContext.agentProfile());
        }

        String templateType = effectiveContext.templateType();

        if (!llmEnabled) {
            return AssistantChatResult.outOfScope(
                    AssistantCapabilitiesCatalog.formatOutOfScopeMessage(
                            authContext.role(), true, agentProfile));
        }

        if (AssistantOutOfScopeDetector.isOutOfScope(userMessage)) {
            return AssistantChatResult.outOfScope(
                    AssistantCapabilitiesCatalog.formatOutOfScopeMessage(
                            authContext.role(), false, agentProfile));
        }

        List<AssistantToolDefinition> tools = toolRegistry.toolsForRoleAndAgent(
                authContext.role(), agentProfile);
        if (tools.isEmpty()) {
            return AssistantChatResult.outOfScope(
                    AssistantCapabilitiesCatalog.formatOutOfScopeMessage(
                            authContext.role(), false, agentProfile));
        }

        return executeLlmToolLoop(
                userMessage.trim(),
                history,
                authContext,
                effectiveContext,
                tools,
                templateType);
    }

    private AssistantChatResult executeLlmToolLoop(String userMessage,
                                                   List<ChatMessage> history,
                                                   AssistantAuthContext authContext,
                                                   AssistantChatContext chatContext,
                                                   List<AssistantToolDefinition> tools,
                                                   String templateType) {
        List<ChatMessage> conversation = buildToolSelectionConversation(
                history, userMessage, chatContext);
        List<AssistantToolStep> steps = new ArrayList<>();
        List<String> formattedParts = new ArrayList<>();
        boolean hitIterationLimit = false;

        for (int iteration = 0; iteration < maxToolIterations; iteration++) {
            ChatCompletionResult selection = chatCompletionPort.complete(
                    new ChatCompletionRequest(conversation, tools));

            if (!selection.hasToolCalls()) {
                if (steps.isEmpty()) {
                    if (normativeRagService.isEnabled()) {
                        var fallbackRag = normativeRagService.tryDirectAnswer(userMessage, templateType);
                        if (fallbackRag.isPresent()) {
                            return fallbackRag.get();
                        }
                    }
                    return AssistantChatResult.outOfScope(
                            AssistantCapabilitiesCatalog.formatOutOfScopeMessage(
                                    authContext.role(), false, chatContext.agentProfile()));
                }
                break;
            }

            ToolCall toolCall = selection.toolCalls().getFirst();
            conversation.add(toAssistantToolCallMessage(selection.toolCalls()));

            ToolExecutionOutcome outcome;
            try {
                outcome = runTool(
                        toolCall.name(),
                        toolCall.argumentsJson(),
                        authContext,
                        chatContext.agentProfile());
            } catch (Exception ex) {
                return AssistantChatResult.outOfScope("No pude completar la consulta: " + ex.getMessage());
            }
            conversation.add(new ChatMessage(ChatRole.TOOL, outcome.rawJson(), toolCall.id()));

            steps.add(new AssistantToolStep(
                    steps.size() + 1,
                    toolCall.name(),
                    outcome.sourceTables(),
                    outcome.success()));
            formattedParts.add(outcome.formattedReply());

            if (iteration == maxToolIterations - 1) {
                hitIterationLimit = true;
                break;
            }
        }

        if (formattedParts.isEmpty()) {
            return AssistantChatResult.outOfScope("No pude completar la consulta.");
        }

        String reply = combineFormattedReplies(formattedParts);
        if (hitIterationLimit && steps.size() >= maxToolIterations) {
            reply = reply + "\n\n(Límite de " + maxToolIterations
                    + " pasos alcanzado; resultados parciales mostrados arriba.)";
        }

        return AssistantChatResult.fromSteps(
                reply,
                AssistantResolutionPath.LLM,
                true,
                steps);
    }

    private AssistantChatResult executeSingleTool(AssistantToolInvocation invocation,
                                                  AssistantResolutionPath path,
                                                  boolean llmInvoked,
                                                  AssistantAuthContext authContext,
                                                  AssistantAgentProfile agentProfile) {
        try {
            ToolExecutionOutcome outcome = runTool(
                    invocation.toolId(),
                    invocation.argumentsJson(),
                    authContext,
                    agentProfile);
            AssistantToolStep step = new AssistantToolStep(
                    1,
                    invocation.toolId(),
                    outcome.sourceTables(),
                    outcome.success());
            return AssistantChatResult.fromSteps(
                    outcome.formattedReply(),
                    path,
                    llmInvoked,
                    List.of(step));
        } catch (Exception ex) {
            return AssistantChatResult.outOfScope("No pude completar la consulta: " + ex.getMessage());
        }
    }

    private ToolExecutionOutcome runTool(String toolId,
                                         String argumentsJson,
                                         AssistantAuthContext authContext,
                                         AssistantAgentProfile agentProfile) throws Exception {
        String json = toolExecutor.execute(toolId, argumentsJson, authContext, agentProfile);
        ToolExecutionResult result = AssistantResponseFormatter.parseToolJson(json, objectMapper);
        String formattedReply = AssistantResponseFormatter.format(result);
        return new ToolExecutionOutcome(
                json,
                formattedReply,
                AssistantToolSourceRegistry.sourceTablesFor(toolId),
                result.ok());
    }

    private static ChatMessage toAssistantToolCallMessage(List<ToolCall> toolCalls) {
        List<ChatToolCall> calls = toolCalls.stream()
                .map(call -> new ChatToolCall(call.id(), call.name(), call.argumentsJson()))
                .toList();
        return new ChatMessage(ChatRole.ASSISTANT, null, null, calls);
    }

    private static String combineFormattedReplies(List<String> parts) {
        if (parts.size() == 1) {
            return parts.getFirst();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                sb.append("\n\n---\n\n");
            }
            sb.append("**Paso ").append(i + 1).append("**\n").append(parts.get(i));
        }
        return sb.toString();
    }

    private List<ChatMessage> buildToolSelectionConversation(List<ChatMessage> history,
                                                             String userMessage,
                                                             AssistantChatContext chatContext) {
        List<ChatMessage> conversation = new ArrayList<>();
        conversation.add(new ChatMessage(
                ChatRole.SYSTEM,
                systemPrompt + TOOL_SELECTION_PROMPT_SUFFIX + RAG_TOOL_HINT
                        + MAX_ITERATIONS_SUFFIX.formatted(maxToolIterations)
                        + normativeRagContextSuffix(userMessage, chatContext)
                        + phasesContextSuffix(chatContext)
                        + usersContextSuffix(chatContext)
                        + evidenceContextSuffix(chatContext)));

        if (history != null) {
            history.stream()
                    .filter(message -> message.role() != ChatRole.SYSTEM)
                    .forEach(conversation::add);
        }

        conversation.add(new ChatMessage(ChatRole.USER, userMessage));
        return conversation;
    }

    private String normativeRagContextSuffix(String userMessage, AssistantChatContext chatContext) {
        if (!normativeRagService.isEnabled()) {
            return "";
        }
        return normativeRagService.buildPromptSuffix(
                userMessage,
                chatContext.templateType());
    }

    private static String phasesContextSuffix(AssistantChatContext chatContext) {
        if (!chatContext.isPhasesAgent()) {
            return "";
        }
        String career = chatContext.careerName() != null ? chatContext.careerName() : chatContext.careerCode();
        String template = chatContext.templateType() != null ? chatContext.templateType() : "CEUB";
        String phases = chatContext.phaseCatalogPrompt() != null && !chatContext.phaseCatalogPrompt().isBlank()
                ? chatContext.phaseCatalogPrompt()
                : "(consulte list_process_structure antes de escribir)";
        return PHASES_AGENT_PROMPT_SUFFIX.formatted(career, template, phases);
    }

    private static String usersContextSuffix(AssistantChatContext chatContext) {
        if (!chatContext.isUsersAgent()) {
            return "";
        }
        StringBuilder extra = new StringBuilder(USERS_AGENT_PROMPT_SUFFIX);
        if (chatContext.focusUserId() != null) {
            extra.append("\n- Usuario en foco (userId): ").append(chatContext.focusUserId());
        }
        if (chatContext.programId() != null) {
            extra.append("\n- Programa en contexto (programId): ").append(chatContext.programId());
        }
        return extra.toString();
    }

    private static String evidenceContextSuffix(AssistantChatContext chatContext) {
        if (!chatContext.isEvidenceAgent()) {
            return "";
        }
        StringBuilder extra = new StringBuilder(EVIDENCE_AGENT_PROMPT_SUFFIX);
        if (chatContext.programId() != null) {
            extra.append("\n- Programa en contexto (programId): ").append(chatContext.programId());
            extra.append("\n- Prefiere pasar programId en list_pending_evidences cuando aplique.");
        }
        return extra.toString();
    }

    private record ToolExecutionOutcome(
            String rawJson,
            String formattedReply,
            List<String> sourceTables,
            boolean success
    ) {
    }
}
