package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectIndicatorRequest(
        @NotBlank
        @Size(min = 20, max = 2000)
        String justification
) {
}
