package com.umss.sigesa.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminNewsResponse(
        UUID id,
        String slug,
        String title,
        String category,
        String summary,
        String body,
        String status,
        Instant publishedAt,
        boolean featured
) {
}
