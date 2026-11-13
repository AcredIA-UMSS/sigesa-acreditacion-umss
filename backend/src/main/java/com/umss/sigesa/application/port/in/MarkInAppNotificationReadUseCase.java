package com.umss.sigesa.application.port.in;

import java.util.UUID;

public interface MarkInAppNotificationReadUseCase {

    /**
     * @return false when notification does not belong to the user (or missing)
     */
    boolean markRead(UUID notificationId, UUID userId);
}
