package com.umss.sigesa.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GenerateExecutiveReportRequest(
        UUID facultyId,
        UUID programId,
        @NotNull Integer managementYear
) {
}
