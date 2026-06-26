package com.umss.sigesa.adapter.out.auth;

import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.domain.model.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NoOpAuditLogAdapter implements AuditLogPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpAuditLogAdapter.class);

    @Override
    public void logLogin(UUID userId, Email email) {
        log.info("AUDIT_LOGIN userId={} email={}", userId, email.value());
    }

    @Override
    public void logUserRegistered(UUID userId, Email email) {
        log.info("AUDIT_USER_REGISTERED userId={} email={}", userId, email.value());
    }

    @Override
    public void logUserDeactivated(UUID userId, Email email) {
        log.info("AUDIT_USER_DEACTIVATED userId={} email={}", userId, email.value());
    }
}
