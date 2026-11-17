package com.umss.sigesa.adapter.in.web.dto;

import com.umss.sigesa.domain.model.observation.ComplianceLevel;
import com.umss.sigesa.domain.model.observation.ObservationKind;
import com.umss.sigesa.domain.model.observation.ObservationPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ObservationDraftRequest(
        @NotNull UUID evidenceId,
        @NotBlank String description,
        @NotNull ObservationKind kind,
        @NotNull ObservationPriority priority,
        @NotNull ComplianceLevel compliance,
        String recommendation
) {
}
