package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.port.in.SendChatMessageUseCase;
import com.umss.sigesa.application.port.out.ChatCompletionPort;
import com.umss.sigesa.domain.model.ChatMessage;
import com.umss.sigesa.domain.model.ChatRole;

import java.util.ArrayList;
import java.util.List;

public class SendChatMessageService implements SendChatMessageUseCase {

    private final ChatCompletionPort chatCompletionPort;
    private final String systemPrompt;

    public SendChatMessageService(ChatCompletionPort chatCompletionPort, String systemPrompt) {
        this.chatCompletionPort = chatCompletionPort;
        this.systemPrompt = systemPrompt;
    }

    @Override
    public String send(String userMessage, List<ChatMessage> history) {
        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío.");
        }

        List<ChatMessage> conversation = new ArrayList<>();
        conversation.add(new ChatMessage(ChatRole.SYSTEM, systemPrompt));

        if (history != null) {
            history.stream()
                    .filter(message -> message.role() != ChatRole.SYSTEM)
                    .forEach(conversation::add);
        }

        conversation.add(new ChatMessage(ChatRole.USER, userMessage.trim()));
        return chatCompletionPort.complete(conversation);
    }
}
