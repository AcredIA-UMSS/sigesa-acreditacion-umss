package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.AuthenticateUseCase;
import com.umss.sigesa.application.port.in.DeactivateUserUseCase;
import com.umss.sigesa.application.port.in.RegisterUserUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.AuthPort;
import com.umss.sigesa.application.port.out.TokenPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.application.service.auth.AuthenticateService;
import com.umss.sigesa.application.service.auth.DeactivateUserService;
import com.umss.sigesa.application.service.auth.RegisterUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthModuleConfig {

    @Bean
    AuthenticateUseCase authenticateUseCase(AuthPort authPort,
                                            TokenPort tokenPort,
                                            UserRepositoryPort userRepository,
                                            AuditLogPort auditLogPort) {
        return new AuthenticateService(authPort, tokenPort, userRepository, auditLogPort);
    }

    @Bean
    RegisterUserUseCase registerUserUseCase(UserRepositoryPort userRepository,
                                            UserProgramAssignmentRepositoryPort assignmentRepository,
                                            AuditLogPort auditLogPort) {
        return new RegisterUserService(userRepository, assignmentRepository, auditLogPort);
    }

    @Bean
    DeactivateUserUseCase deactivateUserUseCase(UserRepositoryPort userRepository,
                                                  UserProgramAssignmentRepositoryPort assignmentRepository,
                                                  AuditLogPort auditLogPort) {
        return new DeactivateUserService(userRepository, assignmentRepository, auditLogPort);
    }
}
