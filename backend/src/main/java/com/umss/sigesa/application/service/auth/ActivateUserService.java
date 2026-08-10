package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.in.ActivateUserUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.InvalidUserStatusTransitionException;
import com.umss.sigesa.domain.exception.UserNotFoundException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.UserStatus;

import java.util.UUID;

public class ActivateUserService implements ActivateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final AuditLogPort auditLogPort;

    public ActivateUserService(UserRepositoryPort userRepository, AuditLogPort auditLogPort) {
        this.userRepository = userRepository;
        this.auditLogPort = auditLogPort;
    }

    @Override
    public void activate(UUID userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new InvalidUserStatusTransitionException("El usuario ya está activo.");
        }

        user.activate();
        userRepository.update(user);
        auditLogPort.logUserActivated(user.getId(), user.getEmail());
    }
}
