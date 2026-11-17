package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeliverableFeedbackRequest(
        @NotBlank @Size(min = 10, max = 4000) String technicalObservations
) {
}
