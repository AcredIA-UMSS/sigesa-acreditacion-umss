package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.LoginRequest;
import com.umss.sigesa.adapter.in.web.dto.LoginResponse;
import com.umss.sigesa.application.access.AccessSessionHolder;
import com.umss.sigesa.application.port.in.AuthenticateUseCase;
import com.umss.sigesa.application.port.out.AccessSessionPort;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.model.access.AccessSessionEndReason;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticateUseCase authenticateUseCase;
    private final AccessSessionPort accessSessionPort;
    private final AuditLogPort auditLogPort;
    private final UserRepositoryPort userRepositoryPort;

    public AuthController(AuthenticateUseCase authenticateUseCase,
                          AccessSessionPort accessSessionPort,
                          AuditLogPort auditLogPort,
                          UserRepositoryPort userRepositoryPort) {
        this.authenticateUseCase = authenticateUseCase;
        this.accessSessionPort = accessSessionPort;
        this.auditLogPort = auditLogPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        AuthenticateUseCase.LoginResult result = authenticateUseCase.authenticate(
                request.email(),
                request.password()
        );

        LoginResponse response = new LoginResponse(
                result.token().accessToken(),
                result.token().expiresInSeconds(),
                result.role().name(),
                result.programScope()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, Authentication authentication) {
        Object sessionId = request.getAttribute(AccessSessionHolder.REQUEST_ATTRIBUTE);
        if (sessionId instanceof UUID id) {
            accessSessionPort.close(id, AccessSessionEndReason.LOGOUT);
        }
        if (authentication != null && authentication.getPrincipal() instanceof UUID userId) {
            userRepositoryPort.findById(userId).ifPresent(user ->
                    auditLogPort.logLogout(userId, user.getEmail(),
                            user.getRole() == null ? null : user.getRole().name()));
        }
        return ResponseEntity.noContent().build();
    }
}
