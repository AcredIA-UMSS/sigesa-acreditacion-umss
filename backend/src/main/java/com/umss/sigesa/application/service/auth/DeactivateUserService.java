package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.in.DeactivateUserUseCase;
import com.umss.sigesa.application.port.out.AccessSessionPort;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.NoOpAccessSessionPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.UserNotFoundException;
import com.umss.sigesa.domain.model.AppUser;

import java.util.UUID;

public class DeactivateUserService implements DeactivateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final AuditLogPort auditLogPort;
    private final AccessSessionPort accessSessionPort;

    public DeactivateUserService(UserRepositoryPort userRepository,
                                 UserProgramAssignmentRepositoryPort assignmentRepository,
                                 AuditLogPort auditLogPort) {
        this(userRepository, assignmentRepository, auditLogPort, NoOpAccessSessionPort.INSTANCE);
    }

    public DeactivateUserService(UserRepositoryPort userRepository,
                                 UserProgramAssignmentRepositoryPort assignmentRepository,
                                 AuditLogPort auditLogPort,
                                 AccessSessionPort accessSessionPort) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.auditLogPort = auditLogPort;
        this.accessSessionPort = accessSessionPort;
    }

    @Override
    public void deactivate(UUID userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.deactivate();
        userRepository.update(user);
        assignmentRepository.revokeAllActiveByUserId(userId);
        accessSessionPort.revokeActiveByUser(userId);
        auditLogPort.logUserDeactivated(user.getId(), user.getEmail());
    }
}
