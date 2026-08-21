package com.umss.sigesa.application.model.assistant;

import java.util.List;

public record AssistantToolStep(
        int step,
        String toolId,
        List<String> sourceTables,
        boolean success
) {
}
