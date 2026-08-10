package com.umss.sigesa.adapter.in.web.dto;

import java.util.List;

public record AssistantStatusResponse(
        boolean enabled,
        boolean llmEnabled,
        String model,
        List<String> capabilities,
        List<AssistantDemoScenarioResponse> demoScenarios
) {
}
