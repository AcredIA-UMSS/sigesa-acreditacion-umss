package com.umss.sigesa.application.service.auth.support;

import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.domain.model.Email;

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
    public void logUserRegistered(UUID userId, Email email) {
        events.add("REGISTER:" + userId);
    }

    @Override
    public void logUserDeactivated(UUID userId, Email email) {
        events.add("DEACTIVATE:" + userId);
    }

    @Override
    public void logReportRequested(UUID requesterId, UUID jobId) {
        events.add("REPORT:" + requesterId + ":" + jobId);
    }

    @Override
    public void logEvidenceUploaded(UUID uploadedBy, UUID evidenceId, UUID indicatorId) {
        events.add("EVIDENCE:" + uploadedBy + ":" + evidenceId + ":" + indicatorId);
    }

    public List<String> events() {
        return List.copyOf(events);
    }
}
