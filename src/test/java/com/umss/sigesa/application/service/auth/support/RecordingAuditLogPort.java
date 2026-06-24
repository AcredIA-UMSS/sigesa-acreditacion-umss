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

    public List<String> events() {
        return List.copyOf(events);
    }
}
