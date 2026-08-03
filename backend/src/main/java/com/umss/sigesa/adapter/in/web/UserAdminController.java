package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.application.port.in.DeactivateUserUseCase;
import com.umss.sigesa.application.port.in.ListUsersUseCase;
import com.umss.sigesa.application.port.in.RegisterUserUseCase;
import com.umss.sigesa.adapter.in.web.dto.RegisterUserRequest;
import com.umss.sigesa.adapter.in.web.dto.RegisterUserResponse;
import com.umss.sigesa.adapter.in.web.dto.UserAdminSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Alta de usuarios por [JD]. La contraseña la define [JD] en el alta;
 * no se almacena en texto plano ni puede recuperarse después.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {

    private final RegisterUserUseCase registerUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final ListUsersUseCase listUsersUseCase;

    public UserAdminController(RegisterUserUseCase registerUserUseCase,
                               DeactivateUserUseCase deactivateUserUseCase,
                               ListUsersUseCase listUsersUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
        this.listUsersUseCase = listUsersUseCase;
    }

    @GetMapping
    public List<UserAdminSummaryResponse> list(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        return listUsersUseCase.list(role, status).stream()
                .map(summary -> new UserAdminSummaryResponse(
                        summary.userId(),
                        summary.email(),
                        summary.role(),
                        summary.status(),
                        summary.programIds(),
                        summary.firstName(),
                        summary.lastName(),
                        summary.fullName(),
                        summary.phoneNumber()
                ))
                .toList();
    }

    @PostMapping
    public ResponseEntity<RegisterUserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        char[] password = request.password().toCharArray();
        try {
            RegisterUserUseCase.RegisterResult result = registerUserUseCase.register(
                    new RegisterUserUseCase.RegisterUserCommand(
                            request.email(),
                            request.role(),
                            request.programId(),
                            request.firstName(),
                            request.lastName(),
                            request.phoneNumber(),
                            password
                    )
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegisterUserResponse(result.userId(), result.status().name()));
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        deactivateUserUseCase.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
