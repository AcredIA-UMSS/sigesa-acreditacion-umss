package com.umss.sigesa.application.port.out;

import java.util.UUID;

public interface NotificationSenderPort {

    void send(UUID notificationId, String recipientEmail, String subject, String body, String deepLink);
}
