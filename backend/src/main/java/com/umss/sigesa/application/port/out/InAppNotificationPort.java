package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.notification.InAppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InAppNotificationPort {

    void save(InAppNotification notification);

    Page<InAppNotification> findByUserId(UUID userId, boolean unreadOnly, Pageable pageable);

    /**
     * @return true if marked (or already read by owner); false if missing / other user
     */
    boolean markRead(UUID notificationId, UUID userId);

    void markAllRead(UUID userId);
}
