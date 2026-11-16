package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NewsWriteRequest(
        @NotBlank(message = "El título es obligatorio.")
        @Size(max = 255, message = "El título no puede superar 255 caracteres.")
        String title,
        @NotNull(message = "La categoría es obligatoria.")
        String category,
        @NotBlank(message = "El resumen es obligatorio.")
        @Size(max = 500, message = "El resumen no puede superar 500 caracteres.")
        String summary,
        String body
) {
}
