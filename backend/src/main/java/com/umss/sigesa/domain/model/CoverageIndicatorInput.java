package com.umss.sigesa.domain.model;

import java.util.UUID;

public record CoverageIndicatorInput(
        UUID dimensionId,
        String dimensionName,
        int dimensionOrder,
        UUID componentId,
        String componentName,
        int componentOrder,
        UUID criterionId,
        String criterionName,
        int criterionOrder,
        UUID indicatorId,
        String code,
        String description,
        String status,
        int evidenceCount,
        int indicatorOrder
) {
}
