package com.umss.sigesa.adapter.out.notification;

import com.umss.sigesa.application.port.out.NotificationSenderPort;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@Primary
@ConditionalOnProperty(name = "spring.mail.host")
public class JavaMailNotificationSenderAdapter implements NotificationSenderPort {

    private static final Logger log = LoggerFactory.getLogger(JavaMailNotificationSenderAdapter.class);

    private final JavaMailSender mailSender;
    private final NotificationEmailTemplateService templateService;
    private final String fromAddress;

    public JavaMailNotificationSenderAdapter(
            JavaMailSender mailSender,
            NotificationEmailTemplateService templateService,
            @Value("${sigesa.mail.from:no-reply@umss.edu.bo}") String fromAddress) {
        this.mailSender = mailSender;
        this.templateService = templateService;
        this.fromAddress = fromAddress;
        log.info("JavaMailNotificationSenderAdapter active from={} (real SMTP)", fromAddress);
    }

    @Override
    public void send(UUID notificationId, String recipientEmail, String subject, String body, String deepLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            String html = templateService.renderHtml(subject, body, deepLink);
            helper.setText(body == null ? "" : body, html);
            mailSender.send(message);
            log.info("DISPATCH_NOTIFICATION [SUCCESS] id={} recipient={} from={} subject='{}'",
                    notificationId, recipientEmail, fromAddress, subject);
        } catch (Exception ex) {
            log.error("DISPATCH_NOTIFICATION [FAIL] id={} recipient={} reason={}",
                    notificationId, recipientEmail, ex.getMessage());
            throw new IllegalStateException("SMTP send failed: " + sanitize(ex.getMessage()), ex);
        }
    }

    String fromAddress() {
        return fromAddress;
    }

    private static String sanitize(String message) {
        if (message == null) {
            return "unknown";
        }
        return message.length() > 400 ? message.substring(0, 400) : message;
    }
}
