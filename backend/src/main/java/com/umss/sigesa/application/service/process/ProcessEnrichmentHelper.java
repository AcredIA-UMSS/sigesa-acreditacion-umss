package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.model.process.ProcessSummary;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateLevel2Node;
import com.umss.sigesa.domain.model.TemplateLevel3Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ProcessEnrichmentHelper {

    private ProcessEnrichmentHelper() {
    }

    public static ProcessSummary toSummary(ProcessQueryPort.ProcessListItem item,
                                           ProgramCatalogPort programCatalogPort,
                                           TemplatePort templatePort,
                                           NormativeHierarchyQueryPort normativeHierarchyQueryPort) {
        ProgramCatalogPort.ProgramEntry program = programCatalogPort.findById(item.careerId())
                .orElse(new ProgramCatalogPort.ProgramEntry(item.careerId(), "", ""));
        Template template = templatePort.findMetadataById(item.templateId()).orElse(null);
        String templateName = template != null ? template.getName() : "";
        String templateType = template != null ? template.getType() : "";
        String evaluatorModel = resolveEvaluatorModel(templateType);

        int level1Count;
        int indicatorCount;
        if (normativeHierarchyQueryPort.hasNormativeTreeForProcess(item.id())) {
            level1Count = (int) normativeHierarchyQueryPort.countLevel1NodesByProcessId(item.id());
            indicatorCount = (int) normativeHierarchyQueryPort.countIndicatorsByProcessId(item.id());
        } else {
            level1Count = item.phaseCount();
            indicatorCount = item.subphaseCount();
        }

        return new ProcessSummary(
                item.id(),
                item.careerId(),
                program.code(),
                program.name(),
                item.templateId(),
                templateName,
                templateType,
                evaluatorModel,
                item.status(),
                item.startDate(),
                item.phaseCount(),
                item.subphaseCount(),
                level1Count,
                indicatorCount,
                null
        );
    }

    public static EnrichedProcessDetail toDetail(AccreditationProcess process,
                                                 ProgramCatalogPort programCatalogPort,
                                                 TemplatePort templatePort,
                                                 NormativeHierarchyQueryPort normativeHierarchyQueryPort) {
        sortPhasesAndSubphases(process);

        ProgramCatalogPort.ProgramEntry program = programCatalogPort.findById(process.getCareerId())
                .orElse(new ProgramCatalogPort.ProgramEntry(process.getCareerId(), "", ""));
        Template template = templatePort.findMetadataById(process.getTemplateId()).orElse(null);
        String templateName = template != null ? template.getName() : "";
        String templateType = template != null ? template.getType() : "";
        String evaluatorModel = resolveEvaluatorModel(templateType);

        List<Level1Node> level1Nodes = normativeHierarchyQueryPort.findProcessTree(process.getId())
                .map(tree -> new ArrayList<>(tree.level1Nodes()))
                .orElseGet(ArrayList::new);
        sortNormativeTree(level1Nodes);

        return new EnrichedProcessDetail(
                process.getId(),
                process.getCareerId(),
                program.code(),
                program.name(),
                process.getTemplateId(),
                templateName,
                templateType,
                evaluatorModel,
                process.getStatus(),
                process.getStartDate(),
                process.getPhases(),
                level1Nodes,
                null
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

    public static void sortNormativeTree(List<Level1Node> level1Nodes) {
        if (level1Nodes == null) {
            return;
        }
        level1Nodes.sort(Comparator.comparing(node -> node.getOrder() != null ? node.getOrder() : 0));
        level1Nodes.forEach(level1 -> {
            if (level1.getLevel2Nodes() != null) {
                level1.setLevel2Nodes(new ArrayList<>(level1.getLevel2Nodes()));
                level1.getLevel2Nodes().sort(Comparator.comparing(node -> node.getOrder() != null ? node.getOrder() : 0));
                level1.getLevel2Nodes().forEach(level2 -> {
                    if (level2.getLevel3Nodes() != null) {
                        level2.setLevel3Nodes(new ArrayList<>(level2.getLevel3Nodes()));
                        level2.getLevel3Nodes().sort(Comparator.comparing(
                                node -> node.getOrder() != null ? node.getOrder() : 0));
                        level2.getLevel3Nodes().forEach(level3 -> {
                            if (level3.getIndicators() != null) {
                                level3.setIndicators(new ArrayList<>(level3.getIndicators()));
                                level3.getIndicators().sort(Comparator.comparing(
                                        indicator -> indicator.getOrder() != null ? indicator.getOrder() : 0));
                            }
                        });
                    }
                });
            }
        });
    }

    public static void sortTemplateNormativeTree(List<TemplateLevel1Node> level1Nodes) {
        if (level1Nodes == null) {
            return;
        }
        level1Nodes.sort(Comparator.comparing(node -> node.getOrder() != null ? node.getOrder() : 0));
        level1Nodes.forEach(level1 -> {
            if (level1.getLevel2Nodes() != null) {
                level1.setLevel2Nodes(new ArrayList<>(level1.getLevel2Nodes()));
                level1.getLevel2Nodes().sort(Comparator.comparing(node -> node.getOrder() != null ? node.getOrder() : 0));
                level1.getLevel2Nodes().forEach(level2 -> {
                    if (level2.getLevel3Nodes() != null) {
                        level2.setLevel3Nodes(new ArrayList<>(level2.getLevel3Nodes()));
                        level2.getLevel3Nodes().sort(Comparator.comparing(
                                node -> node.getOrder() != null ? node.getOrder() : 0));
                        level2.getLevel3Nodes().forEach(level3 -> {
                            if (level3.getIndicators() != null) {
                                level3.setIndicators(new ArrayList<>(level3.getIndicators()));
                                level3.getIndicators().sort(Comparator.comparing(
                                        indicator -> indicator.getOrder() != null ? indicator.getOrder() : 0));
                            }
                        });
                    }
                });
            }
        });
    }

    public static String resolveEvaluatorModel(String templateType) {
        if (templateType == null || templateType.isBlank()) {
            return "";
        }
        return templateType;
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
