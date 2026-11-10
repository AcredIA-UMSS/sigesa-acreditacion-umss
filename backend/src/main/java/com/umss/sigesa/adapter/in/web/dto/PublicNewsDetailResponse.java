package com.umss.sigesa.adapter.in.web.dto;

import java.time.Instant;

public record PublicNewsDetailResponse(
        String slug,
        String title,
        String category,
        String summary,
        String body,
        Instant publishedAt,
        boolean featured
) {
}
