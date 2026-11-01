package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.GetAuditLogsUseCase;
import com.umss.sigesa.application.port.out.AuditLogQueryPort;
import com.umss.sigesa.application.service.audit.GetAuditLogsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AuditModuleConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    GetAuditLogsUseCase getAuditLogsUseCase(AuditLogQueryPort auditLogQueryPort) {
        return new GetAuditLogsService(auditLogQueryPort);
    }
}
