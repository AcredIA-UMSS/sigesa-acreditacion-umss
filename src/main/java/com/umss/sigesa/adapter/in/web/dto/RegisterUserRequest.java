package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record RegisterUserRequest(
        @NotBlank(message = "El correo es obligatorio.")
        String email,
        @NotBlank(message = "El rol es obligatorio.")
        String role,
        UUID programId
) {
}
