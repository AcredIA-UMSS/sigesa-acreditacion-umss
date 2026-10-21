package com.umss.sigesa.application.port.out;

import com.umss.sigesa.application.model.notification.NotificationItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationQueryPort {

    Page<NotificationItem> findNotificationsForUser(
            UUID userId,
            List<UUID> programIds,
            String email,
            boolean unreadOnly,
            Pageable pageable);

    long countUnread(UUID userId, List<UUID> programIds, String email);

    Optional<NotificationItem> findById(UUID notificationId);

    void markAsRead(UUID notificationId);

    void markAllAsRead(UUID userId, List<UUID> programIds, String email);
}
