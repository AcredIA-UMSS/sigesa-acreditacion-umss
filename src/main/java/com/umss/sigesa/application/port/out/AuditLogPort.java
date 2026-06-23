package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.Email;

import java.util.UUID;

public interface AuditLogPort {

    void logLogin(UUID userId, Email email);

    void logUserRegistered(UUID userId, Email email);

    void logUserDeactivated(UUID userId, Email email);
}
