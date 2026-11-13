package com.umss.sigesa.application.service.notification;

import com.umss.sigesa.application.port.in.ListMyInAppNotificationsUseCase;
import com.umss.sigesa.application.port.out.InAppNotificationPort;
import com.umss.sigesa.domain.model.notification.InAppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public class ListMyInAppNotificationsService implements ListMyInAppNotificationsUseCase {

    private final InAppNotificationPort inAppNotificationPort;

    public ListMyInAppNotificationsService(InAppNotificationPort inAppNotificationPort) {
        this.inAppNotificationPort = inAppNotificationPort;
    }

    @Override
    public Page<InAppNotification> list(UUID userId, boolean unreadOnly, Pageable pageable) {
        return inAppNotificationPort.findByUserId(userId, unreadOnly, pageable);
    }
}
