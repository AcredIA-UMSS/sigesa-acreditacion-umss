package com.umss.sigesa.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Envelope uniforme de error REST (#67 / FSD-UC-034).
 * Compatible con el contrato vivo {@code { "error", "message", "details"? }}.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        String error,
        String message,
        Map<String, Object> details
) {

    public static ApiErrorResponse of(String error, String message) {
        return new ApiErrorResponse(error, message, null);
    }

    public static ApiErrorResponse of(String error, String message, Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return of(error, message);
        }
        return new ApiErrorResponse(error, message, details);
    }
}
