package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.in.LogoutUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.model.Email;

import java.util.UUID;

public class LogoutService implements LogoutUseCase {

    private final UserRepositoryPort userRepository;
    private final AuditLogPort auditLogPort;

    public LogoutService(UserRepositoryPort userRepository, AuditLogPort auditLogPort) {
        this.userRepository = userRepository;
        this.auditLogPort = auditLogPort;
    }

    @Override
    public void logout(UUID userId) {
        Email email = userRepository.findById(userId).map(user -> user.getEmail()).orElse(null);
        auditLogPort.logLogout(userId, email);
    }
}
