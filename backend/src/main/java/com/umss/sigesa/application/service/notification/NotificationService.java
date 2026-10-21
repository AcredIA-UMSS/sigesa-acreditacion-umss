package com.umss.sigesa.application.service.notification;

import com.umss.sigesa.application.model.notification.NotificationItem;
import com.umss.sigesa.application.port.in.GetNotificationsUseCase;
import com.umss.sigesa.application.port.out.NotificationQueryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional(readOnly = true)
public class NotificationService implements GetNotificationsUseCase {

    private final NotificationQueryPort notificationQueryPort;

    public NotificationService(NotificationQueryPort notificationQueryPort) {
        this.notificationQueryPort = notificationQueryPort;
    }

    @Override
    public Page<NotificationItem> getNotifications(
            UUID userId,
            List<UUID> programIds,
            String email,
            boolean unreadOnly,
            Pageable pageable) {
        return notificationQueryPort.findNotificationsForUser(userId, programIds, email, unreadOnly, pageable);
    }

    @Override
    public long getUnreadCount(UUID userId, List<UUID> programIds, String email) {
        return notificationQueryPort.countUnread(userId, programIds, email);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId) {
        notificationQueryPort.markAsRead(notificationId);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId, List<UUID> programIds, String email) {
        notificationQueryPort.markAllAsRead(userId, programIds, email);
    }
}
