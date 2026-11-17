package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.access.AccessSessionHolder;
import com.umss.sigesa.application.port.in.AuthenticateUseCase;
import com.umss.sigesa.application.port.out.AccessSessionPort;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.AuthPort;
import com.umss.sigesa.application.port.out.IssuedToken;
import com.umss.sigesa.application.port.out.NoOpAccessSessionPort;
import com.umss.sigesa.application.port.out.TokenPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.InvalidCredentialsException;
import com.umss.sigesa.domain.exception.RoleNotAssignedException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.UserStatus;

import java.util.Arrays;
import java.util.UUID;

public class AuthenticateService implements AuthenticateUseCase {

    private final AuthPort authPort;
    private final TokenPort tokenPort;
    private final UserRepositoryPort userRepository;
    private final AuditLogPort auditLogPort;
    private final AccessSessionPort accessSessionPort;

    public AuthenticateService(AuthPort authPort,
                               TokenPort tokenPort,
                               UserRepositoryPort userRepository,
                               AuditLogPort auditLogPort) {
        this(authPort, tokenPort, userRepository, auditLogPort, NoOpAccessSessionPort.INSTANCE);
    }

    public AuthenticateService(AuthPort authPort,
                               TokenPort tokenPort,
                               UserRepositoryPort userRepository,
                               AuditLogPort auditLogPort,
                               AccessSessionPort accessSessionPort) {
        this.authPort = authPort;
        this.tokenPort = tokenPort;
        this.userRepository = userRepository;
        this.auditLogPort = auditLogPort;
        this.accessSessionPort = accessSessionPort;
    }

    @Override
    public LoginResult authenticate(String email, String password) {
        if (password == null || password.isBlank()) {
            accessSessionPort.recordFailure(email);
            throw new InvalidCredentialsException();
        }
        char[] passwordChars = password.toCharArray();
        try {
            Email emailVo;
            try {
                emailVo = Email.forLogin(email);
            } catch (InvalidCredentialsException ex) {
                accessSessionPort.recordFailure(email);
                throw ex;
            }

            AuthenticatedIdentity identity = authPort.authenticate(emailVo, passwordChars)
                    .orElseThrow(() -> {
                        accessSessionPort.recordFailure(email);
                        return new InvalidCredentialsException();
                    });

            if (identity.role() == null) {
                accessSessionPort.recordFailure(email);
                throw new RoleNotAssignedException();
            }

            AppUser user = userRepository.findById(identity.userId())
                    .orElseThrow(() -> {
                        accessSessionPort.recordFailure(email);
                        return new InvalidCredentialsException();
                    });

            if (user.getStatus() == UserStatus.INACTIVE) {
                user.activate();
                userRepository.update(user);
            }

            UUID sessionId = accessSessionPort.openSuccessful(
                    user.getId(), user.getFullName(), identity.email().value(), identity.role().name());
            if (sessionId != null) {
                AccessSessionHolder.bind(sessionId);
            }
            IssuedToken issuedToken = tokenPort.issue(identity);
            auditLogPort.logLogin(identity.userId(), identity.email());

            return new LoginResult(issuedToken, identity.role(), identity.programScope());
        } finally {
            AccessSessionHolder.clear();
            Arrays.fill(passwordChars, '\0');
        }
    }
}
