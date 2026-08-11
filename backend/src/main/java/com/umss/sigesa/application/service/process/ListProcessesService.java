package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.model.process.ProcessResponsibleInfo;
import com.umss.sigesa.application.model.process.ProcessSummary;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ListProcessesService implements ListProcessesUseCase {

    private final ProcessQueryPort processQueryPort;
    private final ProgramCatalogPort programCatalogPort;
    private final TemplatePort templatePort;
    private final ProcessResponsiblePort processResponsiblePort;
    private final UserRepositoryPort userRepositoryPort;

    public ListProcessesService(ProcessQueryPort processQueryPort,
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
    public List<ProcessSummary> list(ProcessQueryContext ctx) {
        if ("CC".equals(ctx.role()) && (ctx.programScope() == null || ctx.programScope().isEmpty())) {
            return List.of();
        }

        List<ProcessQueryPort.ProcessListItem> items;
        if ("CC".equals(ctx.role())) {
            items = processQueryPort.findSummaryItemsByCareerIds(ctx.programScope());
        } else {
            items = processQueryPort.findAllSummaryItems();
        }

        List<ProcessSummary> summaries = items.stream()
                .map(item -> ProcessEnrichmentHelper.toSummary(item, programCatalogPort, templatePort))
                .toList();

        List<UUID> processIds = summaries.stream().map(ProcessSummary::id).toList();
        Map<UUID, ProcessResponsibleInfo> responsibles = ProcessResponsibleEnrichmentHelper.resolveForProcesses(
                processIds, processResponsiblePort, userRepositoryPort);

        return summaries.stream()
                .map(summary -> new ProcessSummary(
                        summary.id(),
                        summary.careerId(),
                        summary.careerCode(),
                        summary.careerName(),
                        summary.templateId(),
                        summary.templateName(),
                        summary.templateType(),
                        summary.status(),
                        summary.startDate(),
                        summary.phaseCount(),
                        summary.subphaseCount(),
                        responsibles.get(summary.id())
                ))
                .toList();
    }
}
