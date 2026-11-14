package com.umss.sigesa.application.port.in;

import java.util.UUID;

public interface MarkAllInAppNotificationsReadUseCase {

    void markAllRead(UUID userId);
}
