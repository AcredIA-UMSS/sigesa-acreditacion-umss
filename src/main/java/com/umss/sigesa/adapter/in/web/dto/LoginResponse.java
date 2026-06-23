package com.umss.sigesa.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        long expiresIn,
        String role,
        List<UUID> programScope
) {
}
