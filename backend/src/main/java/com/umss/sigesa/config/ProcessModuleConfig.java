package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.CreateProcessUseCase;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.application.service.process.GetProcessDetailService;
import com.umss.sigesa.application.service.process.ListProcessesService;
import com.umss.sigesa.application.usecase.CreateProcessUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessModuleConfig {

    @Bean
    CreateProcessUseCase createProcessUseCase(AccreditationProcessPort accreditationProcessPort,
                                              TemplatePort templatePort,
                                              ProgramCatalogPort programCatalogPort) {
        return new CreateProcessUseCaseImpl(accreditationProcessPort, templatePort, programCatalogPort);
    }

    @Bean
    ListProcessesUseCase listProcessesUseCase(ProcessQueryPort processQueryPort,
                                              ProgramCatalogPort programCatalogPort,
                                              TemplatePort templatePort) {
        return new ListProcessesService(processQueryPort, programCatalogPort, templatePort);
    }

    @Bean
    GetProcessDetailUseCase getProcessDetailUseCase(ProcessQueryPort processQueryPort,
                                                    ProgramCatalogPort programCatalogPort,
                                                    TemplatePort templatePort) {
        return new GetProcessDetailService(processQueryPort, programCatalogPort, templatePort);
    }
}
