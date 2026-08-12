package com.umss.sigesa.adapter.in.web.dto;

import java.util.List;

public record AssistantDemoScenarioResponse(
        int number,
        String title,
        String sampleQuestion,
        String expectedPath
) {
}
