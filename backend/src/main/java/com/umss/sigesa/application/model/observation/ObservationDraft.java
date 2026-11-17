package com.umss.sigesa.application.model.observation;

import com.umss.sigesa.domain.model.observation.ComplianceLevel;
import com.umss.sigesa.domain.model.observation.ObservationKind;
import com.umss.sigesa.domain.model.observation.ObservationPriority;

import java.util.UUID;

public record ObservationDraft(
        UUID evidenceId,
        String description,
        ObservationKind kind,
        ObservationPriority priority,
        ComplianceLevel compliance,
        String recommendation
) {
}
