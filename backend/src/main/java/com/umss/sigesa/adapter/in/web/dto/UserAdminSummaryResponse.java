package com.umss.sigesa.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

public record UserAdminSummaryResponse(
        UUID userId,
        String email,
        String role,
        String status,
        List<UUID> programIds
) {
}
