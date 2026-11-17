package com.umss.sigesa.adapter.in.security;

import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public final class JwtProgramScopeSupport {

    private JwtProgramScopeSupport() {
    }

    public static List<UUID> programScopeFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getDetails() == null) {
            return List.of();
        }
        Object details = authentication.getDetails();
        if (details instanceof List<?> list) {
            return list.stream()
                    .filter(UUID.class::isInstance)
                    .map(UUID.class::cast)
                    .toList();
        }
        return List.of();
    }
}
