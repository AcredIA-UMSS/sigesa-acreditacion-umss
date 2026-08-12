package com.umss.sigesa.application.port.out;

import com.umss.sigesa.application.model.assistant.ChatCompletionRequest;
import com.umss.sigesa.application.model.assistant.ChatCompletionResult;

public interface ChatCompletionPort {

    ChatCompletionResult complete(ChatCompletionRequest request);
}
