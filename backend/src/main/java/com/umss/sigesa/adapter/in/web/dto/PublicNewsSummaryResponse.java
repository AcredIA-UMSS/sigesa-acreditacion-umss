package com.umss.sigesa.adapter.in.web.dto;

import java.time.Instant;

public record PublicNewsSummaryResponse(
        String slug,
        String title,
        String category,
        String summary,
        Instant publishedAt,
        boolean featured
) {
}
