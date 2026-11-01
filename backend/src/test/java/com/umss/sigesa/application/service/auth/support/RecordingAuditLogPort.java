package com.umss.sigesa.application.service.auth.support;

import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.EvidenceAuditDetails;
import com.umss.sigesa.domain.model.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecordingAuditLogPort implements AuditLogPort {

    private final List<String> events = new ArrayList<>();

    @Override
    public void logLogin(UUID userId, Email email) {
        events.add("LOGIN:" + userId);
    }

    @Override
    public void logLoginFailure(String attemptedEmail) {
        events.add("LOGIN_FAILURE:" + attemptedEmail);
    }

    @Override
    public void logLogout(UUID userId, Email email) {
        events.add("LOGOUT:" + userId);
    }

    @Override
    public void logSessionExpired(UUID userId, String email) {
        events.add("SESSION_EXPIRED:" + userId);
    }

    @Override
    public void logUserRegistered(UUID userId, Email email, Role role) {
        events.add("REGISTER:" + userId + ":" + role);
    }

    @Override
    public void logUserDeactivated(UUID userId, Email email) {
        events.add("DEACTIVATE:" + userId);
    }

    @Override
    public void logUserActivated(UUID userId, Email email) {
        events.add("ACTIVATE:" + userId);
    }

    @Override
    public void logReportRequested(UUID requesterId, UUID jobId) {
        events.add("REPORT:" + requesterId + ":" + jobId);
    }

    @Override
    public void logEvidenceUploaded(UUID uploadedBy, UUID evidenceId, EvidenceAuditDetails details) {
        events.add("EVIDENCE:" + uploadedBy + ":" + evidenceId + ":" + details.indicatorId());
    }

    public List<String> events() {
        return List.copyOf(events);
    }
}
