package com.umss.sigesa.domain.model;

import java.util.List;

public record ImportImprovementPlanResult(
        int importedCount,
        int skippedCount,
        List<ImprovementPlanItem> items
) {
}
