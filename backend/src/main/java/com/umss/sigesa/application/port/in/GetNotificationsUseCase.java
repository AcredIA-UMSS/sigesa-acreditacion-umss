package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.model.notification.NotificationItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface GetNotificationsUseCase {

    Page<NotificationItem> getNotifications(
            UUID userId,
            List<UUID> programIds,
            String email,
            boolean unreadOnly,
            Pageable pageable);

    long getUnreadCount(UUID userId, List<UUID> programIds, String email);

    void markAsRead(UUID notificationId);

    void markAllAsRead(UUID userId, List<UUID> programIds, String email);
}
