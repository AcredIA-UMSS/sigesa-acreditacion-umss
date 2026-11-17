package com.umss.sigesa.adapter.in.web.dto;

import com.umss.sigesa.domain.model.access.AccessSession;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record AccessSessionResponse(
        String id,
        String userId,
        String fullName,
        String email,
        String role,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Long durationSeconds,
        String ipAddress,
        String userAgent,
        String status,
        String endReason,
        List<String> actions
) {
    public static AccessSessionResponse from(AccessSession session) {
        return from(session, List.of());
    }

    public static AccessSessionResponse from(AccessSession session, List<String> actions) {
        Long duration = null;
        if (session.startedAt() != null && session.endedAt() != null) {
            duration = Duration.between(session.startedAt(), session.endedAt()).getSeconds();
        }
        return new AccessSessionResponse(
                session.id().toString(),
                session.userId() == null ? null : session.userId().toString(),
                session.fullName(),
                session.email(),
                session.role(),
                session.startedAt(),
                session.endedAt(),
                duration,
                session.ipAddress(),
                session.userAgent(),
                session.status().name(),
                session.endReason() == null ? null : session.endReason().name(),
                actions == null ? List.of() : List.copyOf(actions));
    }
}
