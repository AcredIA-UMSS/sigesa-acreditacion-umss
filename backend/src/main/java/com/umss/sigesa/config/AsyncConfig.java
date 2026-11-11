package com.umss.sigesa.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "reportJobExecutor")
    Executor reportJobExecutor(
            @Value("${sigesa.dossier.executor.core-pool-size:4}") int corePoolSize,
            @Value("${sigesa.dossier.executor.max-pool-size:8}") int maxPoolSize,
            @Value("${sigesa.dossier.executor.queue-capacity:50}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("report-job-");
        executor.initialize();
        return executor;
    }

    @Bean
    AsyncUncaughtExceptionHandler asyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Excepción no capturada en método asíncrono {} con parámetros {}: {}",
                    method.getName(), java.util.Arrays.toString(params), throwable.getMessage(), throwable);
        };
    }
}
