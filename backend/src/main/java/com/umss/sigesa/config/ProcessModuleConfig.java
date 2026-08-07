package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.AddProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.AddProcessSubphaseUseCase;
import com.umss.sigesa.application.port.in.ArchiveTemplateUseCase;
import com.umss.sigesa.application.port.in.CreateProcessUseCase;
import com.umss.sigesa.application.port.in.CreateTemplateUseCase;
import com.umss.sigesa.application.port.in.DeleteProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.DeleteProcessSubphaseUseCase;
import com.umss.sigesa.application.port.in.DeleteTemplateUseCase;
import com.umss.sigesa.application.port.in.DuplicateTemplateUseCase;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.GetTemplateUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.in.ListTemplatesUseCase;
import com.umss.sigesa.application.port.in.PublishTemplateUseCase;
import com.umss.sigesa.application.port.in.ReorderProcessStructureUseCase;
import com.umss.sigesa.application.port.in.UpdateProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.UpdateProcessSubphaseUseCase;
import com.umss.sigesa.application.port.in.UpdateTemplateUseCase;
import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessStructurePort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.SubphaseWorkflowPort;
import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.application.service.process.AddProcessPhaseService;
import com.umss.sigesa.application.service.process.AddProcessSubphaseService;
import com.umss.sigesa.application.service.process.DeleteProcessPhaseService;
import com.umss.sigesa.application.service.process.DeleteProcessSubphaseService;
import com.umss.sigesa.application.service.process.GetProcessDetailService;
import com.umss.sigesa.application.service.process.ListProcessesService;
import com.umss.sigesa.application.service.process.ProcessStructureGuard;
import com.umss.sigesa.application.service.process.ReorderProcessStructureService;
import com.umss.sigesa.application.service.process.UpdateProcessPhaseService;
import com.umss.sigesa.application.service.process.UpdateProcessSubphaseService;
import com.umss.sigesa.application.service.template.ArchiveTemplateService;
import com.umss.sigesa.application.service.template.CreateTemplateService;
import com.umss.sigesa.application.service.template.DeleteTemplateService;
import com.umss.sigesa.application.service.template.DuplicateTemplateService;
import com.umss.sigesa.application.service.template.GetTemplateService;
import com.umss.sigesa.application.service.template.ListTemplatesService;
import com.umss.sigesa.application.service.template.PublishTemplateService;
import com.umss.sigesa.application.service.template.TemplateStructureValidator;
import com.umss.sigesa.application.service.template.UpdateTemplateService;
import com.umss.sigesa.application.usecase.CreateProcessUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessModuleConfig {

    @Bean
    TemplateStructureValidator templateStructureValidator() {
        return new TemplateStructureValidator();
    }

    @Bean
    CreateTemplateUseCase createTemplateUseCase(TemplateManagementPort templateManagementPort,
                                                TemplateStructureValidator templateStructureValidator) {
        return new CreateTemplateService(templateManagementPort, templateStructureValidator);
    }

    @Bean
    UpdateTemplateUseCase updateTemplateUseCase(TemplateManagementPort templateManagementPort,
                                                TemplateStructureValidator templateStructureValidator) {
        return new UpdateTemplateService(templateManagementPort, templateStructureValidator);
    }

    @Bean
    GetTemplateUseCase getTemplateUseCase(TemplateManagementPort templateManagementPort) {
        return new GetTemplateService(templateManagementPort);
    }

    @Bean
    ListTemplatesUseCase listTemplatesUseCase(TemplateManagementPort templateManagementPort) {
        return new ListTemplatesService(templateManagementPort);
    }

    @Bean
    PublishTemplateUseCase publishTemplateUseCase(TemplateManagementPort templateManagementPort,
                                                    TemplateStructureValidator templateStructureValidator) {
        return new PublishTemplateService(templateManagementPort, templateStructureValidator);
    }

    @Bean
    ArchiveTemplateUseCase archiveTemplateUseCase(TemplateManagementPort templateManagementPort) {
        return new ArchiveTemplateService(templateManagementPort);
    }

    @Bean
    DuplicateTemplateUseCase duplicateTemplateUseCase(TemplateManagementPort templateManagementPort) {
        return new DuplicateTemplateService(templateManagementPort);
    }

    @Bean
    DeleteTemplateUseCase deleteTemplateUseCase(TemplateManagementPort templateManagementPort) {
        return new DeleteTemplateService(templateManagementPort);
    }

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

    @Bean
    ProcessStructureGuard processStructureGuard() {
        return new ProcessStructureGuard();
    }

    @Bean
    AddProcessPhaseUseCase addProcessPhaseUseCase(ProcessStructurePort processStructurePort,
                                                  ProcessStructureGuard processStructureGuard) {
        return new AddProcessPhaseService(processStructurePort, processStructureGuard);
    }

    @Bean
    UpdateProcessPhaseUseCase updateProcessPhaseUseCase(ProcessStructurePort processStructurePort,
                                                        ProcessStructureGuard processStructureGuard) {
        return new UpdateProcessPhaseService(processStructurePort, processStructureGuard);
    }

    @Bean
    DeleteProcessPhaseUseCase deleteProcessPhaseUseCase(ProcessStructurePort processStructurePort,
                                                        SubphaseWorkflowPort subphaseWorkflowPort,
                                                        ProcessStructureGuard processStructureGuard) {
        return new DeleteProcessPhaseService(processStructurePort, subphaseWorkflowPort, processStructureGuard);
    }

    @Bean
    AddProcessSubphaseUseCase addProcessSubphaseUseCase(ProcessStructurePort processStructurePort,
                                                        ProcessStructureGuard processStructureGuard) {
        return new AddProcessSubphaseService(processStructurePort, processStructureGuard);
    }

    @Bean
    UpdateProcessSubphaseUseCase updateProcessSubphaseUseCase(ProcessStructurePort processStructurePort,
                                                              ProcessStructureGuard processStructureGuard) {
        return new UpdateProcessSubphaseService(processStructurePort, processStructureGuard);
    }

    @Bean
    DeleteProcessSubphaseUseCase deleteProcessSubphaseUseCase(ProcessStructurePort processStructurePort,
                                                              SubphaseWorkflowPort subphaseWorkflowPort,
                                                              ProcessStructureGuard processStructureGuard) {
        return new DeleteProcessSubphaseService(processStructurePort, subphaseWorkflowPort, processStructureGuard);
    }

    @Bean
    ReorderProcessStructureUseCase reorderProcessStructureUseCase(ProcessStructurePort processStructurePort,
                                                                  ProcessStructureGuard processStructureGuard) {
        return new ReorderProcessStructureService(processStructurePort, processStructureGuard);
    }
}
