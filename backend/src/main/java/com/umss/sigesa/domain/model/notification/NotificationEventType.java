package com.umss.sigesa.domain.model.notification;

public enum NotificationEventType {
    IndicatorRejected,
    IndicatorApproved,
    EvidenceUploaded,
    EvidenceSubsanated,
    DeadlineApproaching,
    ProcessClosed,
    Level1Completed,
    ProcessAssigned,
    ProcessUnassigned;

    public static NotificationEventType fromWire(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("eventType is required");
        }
        return NotificationEventType.valueOf(raw.trim());
    }
}
