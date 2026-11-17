package com.umss.sigesa.application.service.notification;

import com.umss.sigesa.application.port.in.MarkAllInAppNotificationsReadUseCase;
import com.umss.sigesa.application.port.out.InAppNotificationPort;

import java.util.UUID;

public class MarkAllInAppNotificationsReadService implements MarkAllInAppNotificationsReadUseCase {

    private final InAppNotificationPort inAppNotificationPort;

    public MarkAllInAppNotificationsReadService(InAppNotificationPort inAppNotificationPort) {
        this.inAppNotificationPort = inAppNotificationPort;
    }

    @Override
    public void markAllRead(UUID userId) {
        inAppNotificationPort.markAllRead(userId);
    }
}
