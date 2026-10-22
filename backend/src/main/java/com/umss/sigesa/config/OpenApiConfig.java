package com.umss.sigesa.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SIGESA API",
                version = "8.2.0",
                description = "Contrato OpenAPI generado por springdoc desde los controladores Spring "
                        + "(carga y retiro de evidencias FSD-UC-004/005 y resto del API v1)."))
public class OpenApiConfig {
}
