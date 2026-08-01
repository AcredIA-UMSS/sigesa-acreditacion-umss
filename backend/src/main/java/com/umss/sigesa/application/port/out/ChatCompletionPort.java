package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.ChatMessage;

import java.util.List;

public interface ChatCompletionPort {

    String complete(List<ChatMessage> messages);
}
