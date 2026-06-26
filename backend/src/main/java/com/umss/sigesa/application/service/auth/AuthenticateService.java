package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.in.AuthenticateUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.AuthPort;
import com.umss.sigesa.application.port.out.IssuedToken;
import com.umss.sigesa.application.port.out.TokenPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.InvalidCredentialsException;
import com.umss.sigesa.domain.exception.RoleNotAssignedException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.UserStatus;

import java.util.Arrays;

public class AuthenticateService implements AuthenticateUseCase {

    private final AuthPort authPort;
    private final TokenPort tokenPort;
    private final UserRepositoryPort userRepository;
    private final AuditLogPort auditLogPort;

    public AuthenticateService(AuthPort authPort,
                               TokenPort tokenPort,
                               UserRepositoryPort userRepository,
                               AuditLogPort auditLogPort) {
        this.authPort = authPort;
        this.tokenPort = tokenPort;
        this.userRepository = userRepository;
        this.auditLogPort = auditLogPort;
    }

    @Override
    public LoginResult authenticate(String email, String password) {
        if (password == null || password.isBlank()) {
            throw new InvalidCredentialsException();
        }
        char[] passwordChars = password.toCharArray();
        try {
            Email emailVo = Email.forLogin(email);

            AuthenticatedIdentity identity = authPort.authenticate(emailVo, passwordChars)
                    .orElseThrow(InvalidCredentialsException::new);

            if (identity.role() == null) {
                throw new RoleNotAssignedException();
            }

            AppUser user = userRepository.findById(identity.userId())
                    .orElseThrow(InvalidCredentialsException::new);

            if (user.getStatus() == UserStatus.INACTIVE) {
                user.activate();
                userRepository.update(user);
            }

            IssuedToken issuedToken = tokenPort.issue(identity);
            auditLogPort.logLogin(identity.userId(), identity.email());

            return new LoginResult(issuedToken, identity.role(), identity.programScope());
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }
}
