package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegisterUserRequest(
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "Ingrese un correo electrónico válido.")
        @Pattern(
                regexp = "^[^@\\s]+@umss\\.edu\\.bo$",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Solo se permiten correos institucionales @umss.edu.bo."
        )
        String email,
        @NotBlank(message = "El rol es obligatorio.")
        String role,
        UUID programId,
        @NotBlank(message = "Nombre(s) es obligatorio.")
        @Size(max = 100, message = "Nombre(s) no puede superar 100 caracteres.")
        String firstName,
        @NotBlank(message = "Apellido(s) es obligatorio.")
        @Size(max = 100, message = "Apellido(s) no puede superar 100 caracteres.")
        String lastName,
        @NotBlank(message = "El celular es obligatorio.")
        @Pattern(regexp = "^[67]\\d{7}$", message = "El celular debe tener 8 dígitos entre 60000000 y 79999999.")
        String phoneNumber,
        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
                message = "La contraseña debe tener al menos 8 caracteres e incluir letras y números."
        )
        String password
) {
}
