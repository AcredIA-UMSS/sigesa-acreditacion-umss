package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.LoginRequest;
import com.umss.sigesa.adapter.in.web.dto.LoginResponse;
import com.umss.sigesa.application.port.in.AuthenticateUseCase;
import com.umss.sigesa.application.port.in.LogoutUseCase;
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
    private final LogoutUseCase logoutUseCase;

    public AuthController(AuthenticateUseCase authenticateUseCase, LogoutUseCase logoutUseCase) {
        this.authenticateUseCase = authenticateUseCase;
        this.logoutUseCase = logoutUseCase;
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
    public ResponseEntity<Void> logout(Authentication authentication) {
        logoutUseCase.logout((UUID) authentication.getPrincipal());
        return ResponseEntity.noContent().build();
    }
}
