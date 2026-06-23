package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.application.port.in.DeactivateUserUseCase;
import com.umss.sigesa.application.port.in.RegisterUserUseCase;
import com.umss.sigesa.adapter.in.web.dto.RegisterUserRequest;
import com.umss.sigesa.adapter.in.web.dto.RegisterUserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Alta de usuarios por [JD]. La contraseña temporal se genera en servidor y debe
 * entregarse al usuario por canal offline acordado con DUEA (v1.0: no via API).
 */
@RestController
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {

    private static final String TEMP_PASSWORD_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";

    private final RegisterUserUseCase registerUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserAdminController(RegisterUserUseCase registerUserUseCase,
                               DeactivateUserUseCase deactivateUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
    }

    @PostMapping
    public ResponseEntity<RegisterUserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        char[] tempPassword = generateTemporaryPassword();
        try {
            RegisterUserUseCase.RegisterResult result = registerUserUseCase.register(
                    request.email(),
                    request.role(),
                    request.programId(),
                    tempPassword
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegisterUserResponse(result.userId(), result.status().name()));
        } finally {
            java.util.Arrays.fill(tempPassword, '\0');
        }
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        deactivateUserUseCase.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    private char[] generateTemporaryPassword() {
        char[] password = new char[16];
        for (int i = 0; i < password.length; i++) {
            password[i] = TEMP_PASSWORD_ALPHABET.charAt(secureRandom.nextInt(TEMP_PASSWORD_ALPHABET.length()));
        }
        return password;
    }
}
