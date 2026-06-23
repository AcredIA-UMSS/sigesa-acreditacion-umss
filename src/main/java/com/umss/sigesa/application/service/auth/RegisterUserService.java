package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.in.RegisterUserUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.InvalidScopeException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import com.umss.sigesa.domain.model.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final AuditLogPort auditLogPort;

    public RegisterUserService(UserRepositoryPort userRepository,
                               UserProgramAssignmentRepositoryPort assignmentRepository,
                               AuditLogPort auditLogPort) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.auditLogPort = auditLogPort;
    }

    @Override
    public RegisterResult register(String email, Role role, UUID programId, char[] temporaryPassword) {
        Email emailVo = Email.of(email);
        validateScope(role, programId);

        LocalDateTime now = LocalDateTime.now();
        AppUser user = new AppUser(
                UUID.randomUUID(),
                emailVo,
                role,
                UserStatus.INACTIVE,
                now,
                now
        );

        AppUser saved = userRepository.save(user, temporaryPassword);

        if (role == Role.CC && programId != null) {
            UserProgramAssignment assignment = new UserProgramAssignment(
                    UUID.randomUUID(),
                    saved.getId(),
                    programId,
                    now,
                    null
            );
            assignmentRepository.save(assignment);
        }

        auditLogPort.logUserRegistered(saved.getId(), saved.getEmail());
        return new RegisterResult(saved.getId(), saved.getStatus());
    }

    private void validateScope(Role role, UUID programId) {
        if (role == Role.CC && programId == null) {
            throw new InvalidScopeException("El rol [CC] requiere programId.");
        }
    }
}
