package com.umss.sigesa.adapter.in.web.dto;

import java.time.LocalDateTime;

public record AccessActionResponse(
        String id,
        LocalDateTime createdAt,
        String module,
        String action,
        String description,
        String result,
        String actorEmail,
        String actorRole
) {
}
