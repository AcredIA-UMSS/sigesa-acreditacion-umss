package com.umss.sigesa.application.port.out;

import java.time.LocalDate;
import java.util.UUID;

public interface ObservationRepositoryPort {

    void savePendingObservation(PendingObservation observation);

    record PendingObservation(
            String observationId,
            UUID programId,
            String indicatorId,
            String indicatorCode,
            String indicatorTitle,
            String description,
            LocalDate issueDate,
            LocalDate dueDate,
            Integer phaseId,
            String status,
            String remediationUrl
    ) {
    }
}
