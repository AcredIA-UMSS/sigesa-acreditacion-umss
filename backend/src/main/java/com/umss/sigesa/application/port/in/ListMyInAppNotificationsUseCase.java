package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.notification.InAppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ListMyInAppNotificationsUseCase {

    Page<InAppNotification> list(UUID userId, boolean unreadOnly, Pageable pageable);
}
