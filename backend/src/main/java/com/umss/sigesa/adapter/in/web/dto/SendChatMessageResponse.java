package com.umss.sigesa.adapter.in.web.dto;

import java.util.List;

public record SendChatMessageResponse(
        String reply,
        String toolId,
        List<String> sourceTables,
        String path,
        boolean llmInvoked,
        List<AssistantToolStepResponse> steps
) {
}
