package com.umss.sigesa.config;

import org.springframework.context.annotation.Configuration;

/**
 * Creación de procesos: {@link com.umss.sigesa.application.usecase.CreateProcessUseCaseImpl}
 * se registra vía {@code @Service} (UC-003 / main). El bean legacy
 * {@code CreateAccreditationProcessService} fue eliminado en el merge.
 */
@Configuration
public class ProcessModuleConfig {
}
