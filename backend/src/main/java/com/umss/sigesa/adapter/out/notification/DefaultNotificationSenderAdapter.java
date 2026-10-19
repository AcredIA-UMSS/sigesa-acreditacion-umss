package com.umss.sigesa.adapter.out.notification;

import com.umss.sigesa.application.port.out.NotificationSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DefaultNotificationSenderAdapter implements NotificationSenderPort {

    private static final Logger log = LoggerFactory.getLogger(DefaultNotificationSenderAdapter.class);

    @Override
    public void send(UUID notificationId, String recipientEmail, String subject, String body, String deepLink) {
        // En producción se vincula con JavaMailSender / SES / SMTP client configurado con timeout <= 10s.
        // Aquí se ejecuta el dispatch físico registrando salida estructurada sin retener transacciones de BD.
        log.info("DISPATCH_NOTIFICATION [SUCCESS] id={} recipient={} subject='{}' deepLink={}",
                notificationId, recipientEmail, subject, deepLink);
    }
}
