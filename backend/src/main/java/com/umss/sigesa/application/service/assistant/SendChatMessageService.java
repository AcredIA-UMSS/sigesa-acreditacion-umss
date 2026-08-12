package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.model.assistant.AssistantAgentProfile;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantChatContext;
import com.umss.sigesa.application.model.assistant.AssistantChatResult;
import com.umss.sigesa.application.model.assistant.AssistantResolutionPath;
import com.umss.sigesa.application.model.assistant.AssistantToolDefinition;
import com.umss.sigesa.application.model.assistant.AssistantToolInvocation;
import com.umss.sigesa.application.model.assistant.ChatCompletionRequest;
import com.umss.sigesa.application.model.assistant.ChatCompletionResult;
import com.umss.sigesa.application.model.assistant.ToolExecutionResult;
import com.umss.sigesa.application.port.in.SendChatMessageUseCase;
import com.umss.sigesa.application.port.out.ChatCompletionPort;
import com.umss.sigesa.domain.model.ChatMessage;
import com.umss.sigesa.domain.model.ChatRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SendChatMessageService implements SendChatMessageUseCase {

    private static final String TOOL_SELECTION_PROMPT_SUFFIX = """

            MODO SELECCIÓN DE TOOL (obligatorio):
            - Tu única tarea es elegir la tool correcta o no elegir ninguna.
            - NUNCA redactes la respuesta final al usuario ni inventes datos.
            - Si la pregunta puede resolverse con una tool disponible, invoca exactamente UNA tool con argumentos JSON válidos.
            - Sinónimos permitidos: «etapas» = fases del proceso; «carreras» = programas; «proceso en curso» = proceso activo.
            - Si la pregunta es sobre presupuesto, finanzas, clima, noticias u otro tema ajeno a acreditación, responde con content vacío y SIN tool_calls.
            - Si ninguna tool aplica (dato fuera del sistema), responde con content vacío y SIN tool_calls.
            """;

    private static final String PHASES_AGENT_PROMPT_SUFFIX = """

            CONTEXTO COPILOTO DE FASES (obligatorio):
            - El usuario está viendo un proceso de acreditación concreto en pantalla.
            - Usa SIEMPRE careerQuery=%s y templateType=%s en los argumentos de las tools (no pidas la carrera).
            - Solo tools de fases: list_process_phases, list_process_structure (lectura) y manage_process_phase / manage_process_subphase (escritura con confirmación, solo JD/TD).
            - No invoques tools de usuarios, programas ni otros procesos.
            - Fases del proceso (usa phaseOrder o phaseId REAL; NUNCA inventes UUIDs como UUID_FASE_1):
            %s
            - Para crear subfase: action=CREATE, phaseOrder=N (o phaseName), name=..., referenceUrl=https://..., confirmed=false primero; confirmed=true solo tras confirmación del usuario.
            - No envíes el campo order al crear subfases: el sistema informa el último orden existente y asigna el siguiente disponible en la vista previa.
            - «Fase N» identifica la fase contenedora (phaseOrder), NO el orden de la subfase.
            """;

    private static final String USERS_AGENT_PROMPT_SUFFIX = """

            CONTEXTO COPILOTO DE USUARIOS (obligatorio):
            - El usuario es Jefatura DUEA [JD] en /admin/users.
            - Solo tools de usuarios: list_users, get_user_detail, create_user, manage_user_status, manage_user_assignment.
            - No invoques tools de fases ni procesos.
            - create_user / manage_*: confirmed=false primero; confirmed=true solo tras confirmación explícita.
            - Correos solo @umss.edu.bo. CC/EE requieren programId (o programQuery).
            - Alta deja la cuenta INACTIVE hasta el primer acceso.
            """;

    private final ChatCompletionPort chatCompletionPort;
    private final AssistantToolRegistry toolRegistry;
    private final AssistantToolExecutor toolExecutor;
    private final AssistantKeywordRouter keywordRouter;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;
    private final boolean llmEnabled;

    public SendChatMessageService(ChatCompletionPort chatCompletionPort,
                                  AssistantToolRegistry toolRegistry,
                                  AssistantToolExecutor toolExecutor,
                                  AssistantKeywordRouter keywordRouter,
                                  ObjectMapper objectMapper,
                                  String systemPrompt,
                                  boolean llmEnabled) {
        this.chatCompletionPort = chatCompletionPort;
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
        this.keywordRouter = keywordRouter;
        this.objectMapper = objectMapper;
        this.systemPrompt = systemPrompt;
        this.llmEnabled = llmEnabled;
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
            return executeTool(keywordMatch.get(), AssistantResolutionPath.KEYWORD, false, authContext);
        }

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

        List<ChatMessage> conversation = buildToolSelectionConversation(
                history, userMessage.trim(), effectiveContext);
        ChatCompletionResult selection = chatCompletionPort.complete(
                new ChatCompletionRequest(conversation, tools));

        if (!selection.hasToolCalls()) {
            return AssistantChatResult.outOfScope(
                    AssistantCapabilitiesCatalog.formatOutOfScopeMessage(
                            authContext.role(), false, agentProfile));
        }

        var toolCall = selection.toolCalls().getFirst();
        return executeTool(
                new AssistantToolInvocation(toolCall.name(), toolCall.argumentsJson()),
                AssistantResolutionPath.LLM,
                true,
                authContext);
    }

    private AssistantChatResult executeTool(AssistantToolInvocation invocation,
                                              AssistantResolutionPath path,
                                              boolean llmInvoked,
                                              AssistantAuthContext authContext) {
        try {
            String json = toolExecutor.execute(invocation.toolId(), invocation.argumentsJson(), authContext);
            ToolExecutionResult result = AssistantResponseFormatter.parseToolJson(json, objectMapper);
            String reply = AssistantResponseFormatter.format(result);
            return new AssistantChatResult(
                    reply,
                    invocation.toolId(),
                    AssistantToolSourceRegistry.sourceTablesFor(invocation.toolId()),
                    path,
                    llmInvoked);
        } catch (Exception ex) {
            return AssistantChatResult.outOfScope("No pude completar la consulta: " + ex.getMessage());
        }
    }

    private List<ChatMessage> buildToolSelectionConversation(List<ChatMessage> history,
                                                             String userMessage,
                                                             AssistantChatContext chatContext) {
        List<ChatMessage> conversation = new ArrayList<>();
        conversation.add(new ChatMessage(
                ChatRole.SYSTEM,
                systemPrompt + TOOL_SELECTION_PROMPT_SUFFIX
                        + phasesContextSuffix(chatContext)
                        + usersContextSuffix(chatContext)));

        if (history != null) {
            history.stream()
                    .filter(message -> message.role() != ChatRole.SYSTEM)
                    .forEach(conversation::add);
        }

        conversation.add(new ChatMessage(ChatRole.USER, userMessage));
        return conversation;
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
}
