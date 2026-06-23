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
        Email emailVo = Email.of(email);
        char[] passwordChars = password != null ? password.toCharArray() : new char[0];

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
    }
}
