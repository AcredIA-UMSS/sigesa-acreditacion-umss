package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.model.process.ProcessSummary;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;

import java.util.List;

public class ListProcessesService implements ListProcessesUseCase {

    private final ProcessQueryPort processQueryPort;
    private final ProgramCatalogPort programCatalogPort;
    private final TemplatePort templatePort;

    public ListProcessesService(ProcessQueryPort processQueryPort,
                                ProgramCatalogPort programCatalogPort,
                                TemplatePort templatePort) {
        this.processQueryPort = processQueryPort;
        this.programCatalogPort = programCatalogPort;
        this.templatePort = templatePort;
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

        return items.stream()
                .map(item -> ProcessEnrichmentHelper.toSummary(item, programCatalogPort, templatePort))
                .toList();
    }
}
