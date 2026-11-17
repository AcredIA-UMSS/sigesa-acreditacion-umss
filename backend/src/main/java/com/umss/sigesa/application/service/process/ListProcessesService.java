package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.model.process.ProcessResponsibleInfo;
import com.umss.sigesa.application.model.process.ProcessSummary;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.model.Template;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ListProcessesService implements ListProcessesUseCase {

    private final ProcessQueryPort processQueryPort;
    private final ProgramCatalogPort programCatalogPort;
    private final TemplatePort templatePort;
    private final ProcessResponsiblePort processResponsiblePort;
    private final UserRepositoryPort userRepositoryPort;
    private final NormativeHierarchyQueryPort normativeHierarchyQueryPort;

    public ListProcessesService(ProcessQueryPort processQueryPort,
                                ProgramCatalogPort programCatalogPort,
                                TemplatePort templatePort,
                                ProcessResponsiblePort processResponsiblePort,
                                UserRepositoryPort userRepositoryPort,
                                NormativeHierarchyQueryPort normativeHierarchyQueryPort) {
        this.processQueryPort = processQueryPort;
        this.programCatalogPort = programCatalogPort;
        this.templatePort = templatePort;
        this.processResponsiblePort = processResponsiblePort;
        this.userRepositoryPort = userRepositoryPort;
        this.normativeHierarchyQueryPort = normativeHierarchyQueryPort;
    }

    @Override
    public List<ProcessSummary> list(ProcessQueryContext ctx) {
        if (isCareerScoped(ctx.role()) && (ctx.programScope() == null || ctx.programScope().isEmpty())) {
            return List.of();
        }

        List<ProcessQueryPort.ProcessListItem> items;
        if (isCareerScoped(ctx.role())) {
            items = processQueryPort.findSummaryItemsByCareerIds(ctx.programScope());
        } else {
            items = processQueryPort.findAllSummaryItems();
        }

        if (items.isEmpty()) {
            return List.of();
        }

        List<UUID> processIds = items.stream().map(ProcessQueryPort.ProcessListItem::id).toList();
        List<UUID> careerIds = items.stream().map(ProcessQueryPort.ProcessListItem::careerId).distinct().toList();
        List<UUID> templateIds = items.stream().map(ProcessQueryPort.ProcessListItem::templateId).distinct().toList();

        Map<UUID, ProgramCatalogPort.ProgramEntry> programs = programCatalogPort.findByIds(careerIds);
        Map<UUID, Template> templates = templatePort.findMetadataByIds(templateIds);
        Map<UUID, Long> level1Counts = normativeHierarchyQueryPort.countLevel1NodesByProcessIds(processIds);
        Map<UUID, Long> indicatorCounts = normativeHierarchyQueryPort.countIndicatorsByProcessIds(processIds);
        Map<UUID, ProcessResponsibleInfo> responsibles = ProcessResponsibleEnrichmentHelper.resolveForProcesses(
                processIds, processResponsiblePort, userRepositoryPort);

        return items.stream()
                .map(item -> {
                    ProgramCatalogPort.ProgramEntry program = programs.get(item.careerId());
                    String careerCode = program != null ? program.code() : "";
                    String careerName = program != null ? program.name() : "";

                    Template template = templates.get(item.templateId());
                    String templateName = template != null ? template.getName() : "";
                    String templateType = template != null ? template.getType() : "";
                    String evaluatorModel = ProcessEnrichmentHelper.resolveEvaluatorModel(templateType);

                    int level1Count = level1Counts.getOrDefault(item.id(), 0L).intValue();
                    int indicatorCount = indicatorCounts.getOrDefault(item.id(), 0L).intValue();
                    ProcessResponsibleInfo responsible = responsibles.get(item.id());

                    return new ProcessSummary(
                            item.id(),
                            item.careerId(),
                            careerCode,
                            careerName,
                            item.templateId(),
                            templateName,
                            templateType,
                            evaluatorModel,
                            item.status(),
                            item.startDate(),
                            level1Count,
                            indicatorCount,
                            responsible
                    );
                })
                .toList();
    }

    private static boolean isCareerScoped(String role) {
        return "CC".equals(role) || "EE".equals(role) || "CAE_MEMBER".equals(role);
    }
}
