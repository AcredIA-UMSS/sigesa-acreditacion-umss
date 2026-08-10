package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
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
    public AssistantChatResult send(String userMessage, List<ChatMessage> history, AssistantAuthContext authContext) {
        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío.");
        }
        if (authContext == null) {
            throw new IllegalArgumentException("El contexto de autenticación es obligatorio.");
        }

        Optional<AssistantToolInvocation> keywordMatch =
                keywordRouter.resolve(userMessage, history, authContext);
        if (keywordMatch.isPresent()) {
            return executeTool(keywordMatch.get(), AssistantResolutionPath.KEYWORD, false, authContext);
        }

        if (!llmEnabled) {
            return AssistantChatResult.outOfScope(
                    AssistantCapabilitiesCatalog.formatOutOfScopeMessage(authContext.role(), true));
        }

        if (AssistantOutOfScopeDetector.isOutOfScope(userMessage)) {
            return AssistantChatResult.outOfScope(
                    AssistantCapabilitiesCatalog.formatOutOfScopeMessage(authContext.role(), false));
        }

        List<AssistantToolDefinition> tools = toolRegistry.toolsForRole(authContext.role());
        if (tools.isEmpty()) {
            return AssistantChatResult.outOfScope(
                    AssistantCapabilitiesCatalog.formatOutOfScopeMessage(authContext.role(), false));
        }

        List<ChatMessage> conversation = buildToolSelectionConversation(history, userMessage.trim());
        ChatCompletionResult selection = chatCompletionPort.complete(
                new ChatCompletionRequest(conversation, tools));

        if (!selection.hasToolCalls()) {
            return AssistantChatResult.outOfScope(
                    AssistantCapabilitiesCatalog.formatOutOfScopeMessage(authContext.role(), false));
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

    private List<ChatMessage> buildToolSelectionConversation(List<ChatMessage> history, String userMessage) {
        List<ChatMessage> conversation = new ArrayList<>();
        conversation.add(new ChatMessage(ChatRole.SYSTEM, systemPrompt + TOOL_SELECTION_PROMPT_SUFFIX));

        if (history != null) {
            history.stream()
                    .filter(message -> message.role() != ChatRole.SYSTEM)
                    .forEach(conversation::add);
        }

        conversation.add(new ChatMessage(ChatRole.USER, userMessage));
        return conversation;
    }
}
