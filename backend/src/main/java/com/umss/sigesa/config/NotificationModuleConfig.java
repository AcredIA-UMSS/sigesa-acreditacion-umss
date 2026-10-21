package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.GetNotificationsUseCase;
import com.umss.sigesa.application.port.out.NotificationQueryPort;
import com.umss.sigesa.application.service.notification.NotificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationModuleConfig {

    @Bean
    GetNotificationsUseCase getNotificationsUseCase(NotificationQueryPort notificationQueryPort) {
        return new NotificationService(notificationQueryPort);
    }
}
