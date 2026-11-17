package com.umss.sigesa.domain.model;

public record NotificationModuleSnapshot(
        long pendingCount,
        long totalCount,
        String origin
) {
}
