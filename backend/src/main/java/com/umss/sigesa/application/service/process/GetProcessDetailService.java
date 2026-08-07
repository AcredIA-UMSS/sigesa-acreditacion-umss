package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.model.process.ProcessResponsibleInfo;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;

import java.util.UUID;

public class GetProcessDetailService implements GetProcessDetailUseCase {

    private final ProcessQueryPort processQueryPort;
    private final ProgramCatalogPort programCatalogPort;
    private final TemplatePort templatePort;
    private final ProcessResponsiblePort processResponsiblePort;
    private final UserRepositoryPort userRepositoryPort;

    public GetProcessDetailService(ProcessQueryPort processQueryPort,
                                   ProgramCatalogPort programCatalogPort,
                                   TemplatePort templatePort,
                                   ProcessResponsiblePort processResponsiblePort,
                                   UserRepositoryPort userRepositoryPort) {
        this.processQueryPort = processQueryPort;
        this.programCatalogPort = programCatalogPort;
        this.templatePort = templatePort;
        this.processResponsiblePort = processResponsiblePort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public EnrichedProcessDetail getDetail(UUID processId, ProcessQueryContext ctx) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));

        ProcessAccessPolicy.assertCanAccess(ctx.role(), process.getCareerId(), ctx.programScope(), processId);

        EnrichedProcessDetail detail = ProcessEnrichmentHelper.toDetail(process, programCatalogPort, templatePort);
        ProcessResponsibleInfo responsible = ProcessResponsibleEnrichmentHelper
                .resolveForProcess(processId, processResponsiblePort, userRepositoryPort)
                .orElse(null);

        return new EnrichedProcessDetail(
                detail.id(),
                detail.careerId(),
                detail.careerCode(),
                detail.careerName(),
                detail.templateId(),
                detail.templateName(),
                detail.templateType(),
                detail.status(),
                detail.startDate(),
                detail.phases(),
                responsible
        );
    }
}
