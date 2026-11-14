package com.umss.sigesa.application.service.notification;

import com.umss.sigesa.application.port.in.MarkInAppNotificationReadUseCase;
import com.umss.sigesa.application.port.out.InAppNotificationPort;

import java.util.UUID;

public class MarkInAppNotificationReadService implements MarkInAppNotificationReadUseCase {

    private final InAppNotificationPort inAppNotificationPort;

    public MarkInAppNotificationReadService(InAppNotificationPort inAppNotificationPort) {
        this.inAppNotificationPort = inAppNotificationPort;
    }

    @Override
    public boolean markRead(UUID notificationId, UUID userId) {
        return inAppNotificationPort.markRead(notificationId, userId);
    }
}
