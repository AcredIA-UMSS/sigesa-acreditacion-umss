package com.umss.sigesa.adapter.in.web.dto;

public record AccessSessionSummaryResponse(
        long total,
        long activeUsers,
        long today,
        long failed
) {
}
