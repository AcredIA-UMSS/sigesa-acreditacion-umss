package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantChatResult;
import com.umss.sigesa.domain.model.ChatMessage;

import java.util.List;

public interface SendChatMessageUseCase {

    AssistantChatResult send(String userMessage, List<ChatMessage> history, AssistantAuthContext authContext);
}
