package com.umss.sigesa.adapter.in.web.dto;

import java.util.List;

public record AssistantToolStepResponse(
        int step,
        String toolId,
        List<String> sourceTables,
        boolean success
) {
}
