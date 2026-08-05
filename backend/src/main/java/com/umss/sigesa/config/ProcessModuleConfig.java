package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.CreateProcessUseCase;
import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.application.usecase.CreateProcessUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creación de procesos. {@code SigesaApplication} no escanea {@code application},
 * así que {@link CreateProcessUseCaseImpl} se registra como bean aquí.
 */
@Configuration
public class ProcessModuleConfig {

    @Bean
    CreateProcessUseCase createProcessUseCase(
            AccreditationProcessPort accreditationProcessPort,
            TemplatePort templatePort) {
        return new CreateProcessUseCaseImpl(accreditationProcessPort, templatePort);
    }
}

