package com.umss.sigesa.adapter.out.notification;

import com.umss.sigesa.application.port.out.NotificationSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Always-available log-only fallback for {@link NotificationSenderPort}.
 * When {@code spring.mail.host} is set, {@link JavaMailNotificationSenderAdapter}
 * is registered as {@code @Primary} and takes precedence for injection.
 */
@Component
public class DefaultNotificationSenderAdapter implements NotificationSenderPort {

    private static final Logger log = LoggerFactory.getLogger(DefaultNotificationSenderAdapter.class);

    @Override
    public void send(UUID notificationId, String recipientEmail, String subject, String body, String deepLink) {
        log.info("DISPATCH_NOTIFICATION [SUCCESS] id={} recipient={} subject='{}' deepLink={} (log-only fallback)",
                notificationId, recipientEmail, subject, deepLink);
    }
}
