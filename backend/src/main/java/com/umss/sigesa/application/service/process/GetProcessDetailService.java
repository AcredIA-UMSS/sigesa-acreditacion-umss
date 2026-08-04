package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;

import java.util.UUID;

public class GetProcessDetailService implements GetProcessDetailUseCase {

    private final ProcessQueryPort processQueryPort;
    private final ProgramCatalogPort programCatalogPort;
    private final TemplatePort templatePort;

    public GetProcessDetailService(ProcessQueryPort processQueryPort,
                                   ProgramCatalogPort programCatalogPort,
                                   TemplatePort templatePort) {
        this.processQueryPort = processQueryPort;
        this.programCatalogPort = programCatalogPort;
        this.templatePort = templatePort;
    }

    @Override
    public EnrichedProcessDetail getDetail(UUID processId, ProcessQueryContext ctx) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));

        ProcessAccessPolicy.assertCanAccess(ctx.role(), process.getCareerId(), ctx.programScope(), processId);

        return ProcessEnrichmentHelper.toDetail(process, programCatalogPort, templatePort);
    }
}
