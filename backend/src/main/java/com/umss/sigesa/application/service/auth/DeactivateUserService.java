package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.in.DeactivateUserUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.UserNotFoundException;
import com.umss.sigesa.domain.model.AppUser;

import java.util.UUID;

public class DeactivateUserService implements DeactivateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final AuditLogPort auditLogPort;

    public DeactivateUserService(UserRepositoryPort userRepository,
                                 UserProgramAssignmentRepositoryPort assignmentRepository,
                                 AuditLogPort auditLogPort) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.auditLogPort = auditLogPort;
    }

    @Override
    public void deactivate(UUID userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.deactivate();
        userRepository.update(user);
        assignmentRepository.revokeAllActiveByUserId(userId);
        auditLogPort.logUserDeactivated(user.getId(), user.getEmail());
    }
}
