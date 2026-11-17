package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ObservationCommentRequest(@NotBlank String body) {
}
