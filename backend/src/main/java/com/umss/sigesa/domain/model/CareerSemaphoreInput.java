package com.umss.sigesa.domain.model;

public record CareerSemaphoreInput(
        String programId,
        String name,
        String faculty,
        int totalIndicators,
        double progressPercentage,
        int pendingObservations,
        int overdueObservations
) {
}
