package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ChatMessage;

import java.util.List;

public interface SendChatMessageUseCase {

    String send(String userMessage, List<ChatMessage> history);
}
