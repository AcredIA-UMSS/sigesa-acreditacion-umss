package com.umss.sigesa.domain.model;

import java.util.UUID;

public record PendingIndicator(
        UUID indicatorId,
        String code,
        String name,
        IndicatorState status,
        Integer order
) {
}
