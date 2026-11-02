package com.umss.sigesa.extraction;

import com.umss.sigesa.adapter.out.persistance.NotificationOutboxJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.NotificationOutboxJpaEntity;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Segundo punto de entrada (misma imagen): solo el módulo de notificaciones.
 * No usa {@code @SpringBootApplication} para no duplicar {@code @SpringBootConfiguration}
 * con {@link com.umss.sigesa.SigesaApplication}.
 * Arranque: {@code SIGESA_APP=notification-service}.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = NotificationServiceApplication.class)
@EnableJpaRepositories(basePackageClasses = NotificationOutboxJpaRepository.class)
@EntityScan(basePackageClasses = NotificationOutboxJpaEntity.class)
public class NotificationServiceApplication {
}
