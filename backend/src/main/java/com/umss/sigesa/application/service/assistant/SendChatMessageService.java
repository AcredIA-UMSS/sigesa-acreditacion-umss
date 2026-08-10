package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantToolDefinition;
import com.umss.sigesa.application.model.assistant.ChatCompletionRequest;
import com.umss.sigesa.application.model.assistant.ChatCompletionResult;
import com.umss.sigesa.application.model.assistant.ToolCall;
import com.umss.sigesa.application.port.in.SendChatMessageUseCase;
import com.umss.sigesa.application.port.out.ChatCompletionPort;
import com.umss.sigesa.domain.model.ChatMessage;
import com.umss.sigesa.domain.model.ChatRole;
import com.umss.sigesa.domain.model.ChatToolCall;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SendChatMessageService implements SendChatMessageUseCase {

    private static final String MAX_ITERATIONS_FALLBACK =
            "No pude completar la consulta en el número máximo de pasos. Intente reformular su pregunta.";

    private final ChatCompletionPort chatCompletionPort;
    private final AssistantToolRegistry toolRegistry;
    private final AssistantToolExecutor toolExecutor;
    private final AssistantDirectQueryService directQueryService;
    private final String systemPrompt;
    private final int maxToolIterations;

    public SendChatMessageService(ChatCompletionPort chatCompletionPort,
                                  AssistantToolRegistry toolRegistry,
                                  AssistantToolExecutor toolExecutor,
                                  AssistantDirectQueryService directQueryService,
                                  String systemPrompt,
                                  int maxToolIterations) {
        this.chatCompletionPort = chatCompletionPort;
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
        this.directQueryService = directQueryService;
        this.systemPrompt = systemPrompt;
        this.maxToolIterations = maxToolIterations;
    }

    @Override
    public String send(String userMessage, List<ChatMessage> history, AssistantAuthContext authContext) {
        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío.");
        }
        if (authContext == null) {
            throw new IllegalArgumentException("El contexto de autenticación es obligatorio.");
        }

        List<ChatMessage> conversation = buildConversation(history, userMessage);
        List<AssistantToolDefinition> tools = toolRegistry.toolsForRole(authContext.role());

        Optional<String> directReply = directQueryService.tryHandle(userMessage, history, authContext);
        if (directReply.isPresent()) {
            return directReply.get();
        }

        for (int iteration = 0; iteration < maxToolIterations; iteration++) {
            ChatCompletionResult result = chatCompletionPort.complete(
                    new ChatCompletionRequest(conversation, tools));

            if (!result.hasToolCalls()) {
                return requireNonBlankContent(result.content());
            }

            conversation.add(toAssistantToolCallMessage(result.toolCalls()));

            for (ToolCall call : result.toolCalls()) {
                String toolJson = toolExecutor.execute(call.name(), call.argumentsJson(), authContext);
                conversation.add(new ChatMessage(ChatRole.TOOL, toolJson, call.id()));
            }
        }

        return MAX_ITERATIONS_FALLBACK;
    }

    private List<ChatMessage> buildConversation(List<ChatMessage> history, String userMessage) {
        List<ChatMessage> conversation = new ArrayList<>();
        conversation.add(new ChatMessage(ChatRole.SYSTEM, systemPrompt));

        if (history != null) {
            history.stream()
                    .filter(message -> message.role() != ChatRole.SYSTEM)
                    .forEach(conversation::add);
        }

        conversation.add(new ChatMessage(ChatRole.USER, userMessage.trim()));
        return conversation;
    }

    private static ChatMessage toAssistantToolCallMessage(List<ToolCall> toolCalls) {
        List<ChatToolCall> domainToolCalls = toolCalls.stream()
                .map(call -> new ChatToolCall(call.id(), call.name(), call.argumentsJson()))
                .toList();
        return new ChatMessage(ChatRole.ASSISTANT, null, null, domainToolCalls);
    }

    private static String requireNonBlankContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("El asistente no devolvió contenido en la respuesta.");
        }
        return content;
    }
}
