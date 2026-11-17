package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.CloseAccreditationProcessUseCase;
import com.umss.sigesa.application.port.in.EnqueueDomainNotificationUseCase;
import com.umss.sigesa.application.port.in.ListMyInAppNotificationsUseCase;
import com.umss.sigesa.application.port.in.MarkAllInAppNotificationsReadUseCase;
import com.umss.sigesa.application.port.in.MarkInAppNotificationReadUseCase;
import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.application.port.out.InAppNotificationPort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NotificationOutboxWritePort;
import com.umss.sigesa.application.port.out.NotificationRecipientResolverPort;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.application.service.notification.CloseAccreditationProcessService;
import com.umss.sigesa.application.service.notification.EnqueueDomainNotificationService;
import com.umss.sigesa.application.service.notification.ListMyInAppNotificationsService;
import com.umss.sigesa.application.service.notification.MarkAllInAppNotificationsReadService;
import com.umss.sigesa.application.service.notification.MarkInAppNotificationReadService;
import com.umss.sigesa.application.service.notification.NotificationRecipientResolverService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class NotificationModuleConfig {

    @Bean
    NotificationRecipientResolverPort notificationRecipientResolverPort(
            UserRepositoryPort userRepositoryPort,
            UserProgramAssignmentRepositoryPort assignmentRepositoryPort,
            ProcessResponsiblePort processResponsiblePort) {
        return new NotificationRecipientResolverService(
                userRepositoryPort, assignmentRepositoryPort, processResponsiblePort);
    }

    @Bean
    EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase(
            NotificationRecipientResolverPort recipientResolverPort,
            NotificationOutboxWritePort outboxWritePort,
            InAppNotificationPort inAppNotificationPort,
            AccreditationProcessPort processPort,
            ProgramCatalogPort programCatalogPort,
            TemplatePort templatePort,
            NormativeHierarchyQueryPort normativeHierarchyQueryPort) {
        return new EnqueueDomainNotificationService(
                recipientResolverPort,
                outboxWritePort,
                inAppNotificationPort,
                processPort,
                programCatalogPort,
                templatePort,
                normativeHierarchyQueryPort);
    }

    @Bean
    CloseAccreditationProcessUseCase closeAccreditationProcessUseCase(
            AccreditationProcessPort processPort,
            EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase) {
        return new CloseAccreditationProcessService(processPort, enqueueDomainNotificationUseCase);
    }

    @Bean
    ListMyInAppNotificationsUseCase listMyInAppNotificationsUseCase(InAppNotificationPort inAppNotificationPort) {
        return new ListMyInAppNotificationsService(inAppNotificationPort);
    }

    @Bean
    MarkInAppNotificationReadUseCase markInAppNotificationReadUseCase(InAppNotificationPort inAppNotificationPort) {
        return new MarkInAppNotificationReadService(inAppNotificationPort);
    }

    @Bean
    MarkAllInAppNotificationsReadUseCase markAllInAppNotificationsReadUseCase(
            InAppNotificationPort inAppNotificationPort) {
        return new MarkAllInAppNotificationsReadService(inAppNotificationPort);
    }

    /**
     * Ensures a {@link JavaMailSender} exists when {@code spring.mail.host} is set.
     * Boot autoconfig normally creates this; this bean is a safety net so EMAIL
     * never silently falls back to the log-only sender in Docker.
     */
    @Bean
    @ConditionalOnProperty(name = "spring.mail.host")
    @ConditionalOnMissingBean(JavaMailSender.class)
    JavaMailSender javaMailSender(
            @Value("${spring.mail.host}") String host,
            @Value("${spring.mail.port:2525}") int port,
            @Value("${spring.mail.username:}") String username,
            @Value("${spring.mail.password:}") String password,
            @Value("${spring.mail.properties.mail.smtp.auth:false}") String smtpAuth,
            @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}") String startTls) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        if (username != null && !username.isBlank()) {
            sender.setUsername(username);
        }
        if (password != null && !password.isBlank()) {
            sender.setPassword(password);
        }
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", startTls);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        return sender;
    }
}
