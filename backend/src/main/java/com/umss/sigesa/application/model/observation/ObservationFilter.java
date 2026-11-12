package com.umss.sigesa.application.model.observation;

import com.umss.sigesa.domain.model.observation.ObservationPriority;
import com.umss.sigesa.domain.model.observation.ObservationStatus;

import java.util.UUID;

public record ObservationFilter(
        ObservationStatus status,
        ObservationPriority priority,
        UUID evidenceId,
        UUID authorId
) {
}
