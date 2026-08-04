package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.model.process.ProcessSummary;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;
import com.umss.sigesa.domain.model.Template;

import java.util.ArrayList;
import java.util.Comparator;

public final class ProcessEnrichmentHelper {

    private ProcessEnrichmentHelper() {
    }

    public static ProcessSummary toSummary(ProcessQueryPort.ProcessListItem item,
                                           ProgramCatalogPort programCatalogPort,
                                           TemplatePort templatePort) {
        ProgramCatalogPort.ProgramEntry program = programCatalogPort.findById(item.careerId())
                .orElse(new ProgramCatalogPort.ProgramEntry(item.careerId(), "", ""));
        Template template = templatePort.findById(item.templateId()).orElse(null);
        String templateName = template != null ? template.getName() : "";
        String templateType = template != null ? template.getType() : "";

        return new ProcessSummary(
                item.id(),
                item.careerId(),
                program.code(),
                program.name(),
                item.templateId(),
                templateName,
                templateType,
                item.status(),
                item.startDate(),
                item.phaseCount(),
                item.subphaseCount()
        );
    }

    public static EnrichedProcessDetail toDetail(AccreditationProcess process,
                                                 ProgramCatalogPort programCatalogPort,
                                                 TemplatePort templatePort) {
        sortPhasesAndSubphases(process);

        ProgramCatalogPort.ProgramEntry program = programCatalogPort.findById(process.getCareerId())
                .orElse(new ProgramCatalogPort.ProgramEntry(process.getCareerId(), "", ""));
        Template template = templatePort.findById(process.getTemplateId()).orElse(null);
        String templateName = template != null ? template.getName() : "";
        String templateType = template != null ? template.getType() : "";

        return new EnrichedProcessDetail(
                process.getId(),
                process.getCareerId(),
                program.code(),
                program.name(),
                process.getTemplateId(),
                templateName,
                templateType,
                process.getStatus(),
                process.getStartDate(),
                process.getPhases()
        );
    }

    public static void sortPhasesAndSubphases(AccreditationProcess process) {
        if (process.getPhases() == null) {
            return;
        }
        process.setPhases(new ArrayList<>(process.getPhases()));
        process.getPhases().sort(Comparator.comparing(PhaseOrder::orderOf));
        process.getPhases().forEach(phase -> {
            if (phase.getSubphases() != null) {
                phase.setSubphases(new ArrayList<>(phase.getSubphases()));
                phase.getSubphases().sort(Comparator.comparing(PhaseOrder::orderOfSubphase));
            }
        });
    }

    private static final class PhaseOrder {
        static Integer orderOf(Phase phase) {
            return phase.getOrder() != null ? phase.getOrder() : 0;
        }

        static Integer orderOfSubphase(Subphase subphase) {
            return subphase.getOrder() != null ? subphase.getOrder() : 0;
        }
    }
}
