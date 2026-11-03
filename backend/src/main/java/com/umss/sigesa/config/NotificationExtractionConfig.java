package com.umss.sigesa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.adapter.out.notification.HttpNotificationModuleAdapter;
import com.umss.sigesa.adapter.out.notification.InProcessNotificationModuleAdapter;
import com.umss.sigesa.adapter.out.persistance.NotificationOutboxJpaRepository;
import com.umss.sigesa.application.port.in.NotificationModulePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class NotificationExtractionConfig {

    @Bean
    @ConditionalOnProperty(name = "sigesa.extraction.mode", havingValue = "in_process", matchIfMissing = true)
    NotificationModulePort inProcessNotificationModule(NotificationOutboxJpaRepository outboxRepository) {
        return new InProcessNotificationModuleAdapter(outboxRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "sigesa.extraction.mode", havingValue = "http")
    NotificationModulePort httpNotificationModule(
            @Value("${sigesa.extraction.http.base-url}") String baseUrl,
            @Value("${sigesa.extraction.http.timeout-ms:200}") int timeoutMs,
            @Value("${sigesa.extraction.http.retries:2}") int retries,
            @Value("${sigesa.extraction.http.retry-backoff-ms:50}") int retryBackoffMs,
            @Value("${sigesa.extraction.http.circuit-failures:5}") int circuitFailures,
            @Value("${sigesa.extraction.http.circuit-open-ms:2000}") int circuitOpenMs) {
        Duration timeout = Duration.ofMillis(timeoutMs);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        return new HttpNotificationModuleAdapter(
                client,
                new ObjectMapper(),
                baseUrl,
                timeout,
                retries,
                Duration.ofMillis(retryBackoffMs),
                circuitFailures,
                Duration.ofMillis(circuitOpenMs));
    }
}
