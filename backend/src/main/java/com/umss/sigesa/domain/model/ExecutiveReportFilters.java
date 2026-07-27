package com.umss.sigesa.domain.model;

import java.util.UUID;

public record ExecutiveReportFilters(
        UUID facultyId,
        UUID programId,
        int managementYear
) {
}
