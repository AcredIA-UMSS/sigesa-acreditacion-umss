package com.umss.sigesa.domain.model;

import java.time.LocalDate;

public record ObservationSummary(
        String observationId,
        String indicatorId,
        String indicatorCode,
        String indicatorTitle,
        String description,
        LocalDate issueDate,
        LocalDate dueDate,
        Long remainingDays,
        String status,
        String remediationUrl
) {}
