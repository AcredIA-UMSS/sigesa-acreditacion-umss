package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.model.process.ProcessSummary;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.in.ListProgramsUseCase;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;
import com.umss.sigesa.domain.model.ProcessStatus;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

final class AssistantProcessResolver {

    private AssistantProcessResolver() {
    }

    record ResolvedProcess(
            UUID processId,
            String careerName,
            String careerCode,
            String templateType,
            String templateName
    ) {
    }

    record ResolveResult(ResolvedProcess process, EnrichedProcessDetail detail, String errorCode, String errorMessage) {
        static ResolveResult ok(ResolvedProcess process, EnrichedProcessDetail detail) {
            return new ResolveResult(process, detail, null, null);
        }

        static ResolveResult error(String code, String message) {
            return new ResolveResult(null, null, code, message);
        }

        boolean isOk() {
            return process != null;
        }
    }

    static ResolveResult resolveActiveProcess(String careerQuery,
                                              String templateType,
                                              AssistantAuthContext auth,
                                              ListProgramsUseCase listProgramsUseCase,
                                              ListProcessesUseCase listProcessesUseCase,
                                              GetProcessDetailUseCase getProcessDetailUseCase) {
        AssistantProcessQueryParser.ParsedProcessQuery parsed =
                AssistantProcessQueryParser.parse(careerQuery == null ? "" : careerQuery);
        String effectiveCareerQuery = parsed.careerQuery() != null ? parsed.careerQuery() : careerQuery;
        final String effectiveTemplateType = AssistantProcessQueryParser.normalizeTemplateType(
                templateType != null ? templateType : parsed.templateType());

        if (effectiveCareerQuery == null || effectiveCareerQuery.isBlank()) {
            return ResolveResult.error("INVALID_ARGUMENTS", "Debe indicar el nombre o código de la carrera.");
        }

        String normalizedQuery = normalize(effectiveCareerQuery);
        List<ListProgramsUseCase.ProgramSummary> programs = listProgramsUseCase.list(effectiveCareerQuery.trim());
        List<ListProgramsUseCase.ProgramSummary> matches = programs.stream()
                .filter(program -> normalize(program.name()).contains(normalizedQuery)
                        || normalize(program.code()).contains(normalizedQuery))
                .toList();

        if (matches.isEmpty()) {
            return ResolveResult.error("CAREER_NOT_FOUND", "No se encontró una carrera con ese nombre o código.");
        }
        if (matches.size() > 1) {
            return ResolveResult.error(
                    "AMBIGUOUS_CAREER",
                    "Hay varias carreras que coinciden. Sea más específico o use el código de carrera.");
        }

        ListProgramsUseCase.ProgramSummary program = matches.getFirst();
        ProcessQueryContext ctx = new ProcessQueryContext(auth.role(), auth.programScope());
        List<ProcessSummary> activeProcesses = listProcessesUseCase.list(ctx).stream()
                .filter(summary -> summary.careerId().equals(program.id()))
                .filter(summary -> ProcessStatus.ACTIVE.name().equals(summary.status()))
                .filter(summary -> matchesTemplateType(summary, effectiveTemplateType))
                .toList();

        if (activeProcesses.isEmpty()) {
            List<ProcessSummary> anyActiveForCareer = listProcessesUseCase.list(ctx).stream()
                    .filter(summary -> summary.careerId().equals(program.id()))
                    .filter(summary -> ProcessStatus.ACTIVE.name().equals(summary.status()))
                    .toList();

            if (anyActiveForCareer.isEmpty()) {
                return ResolveResult.error(
                        "ACTIVE_PROCESS_NOT_FOUND",
                        "No hay un proceso de acreditación activo para " + program.name() + ".");
            }

            String available = anyActiveForCareer.stream()
                    .map(summary -> summary.templateType() + " (" + summary.templateName() + ")")
                    .distinct()
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            String requested = effectiveTemplateType != null ? effectiveTemplateType : "indicado";
            return ResolveResult.error(
                    "ACTIVE_PROCESS_NOT_FOUND",
                    "No hay proceso ACTIVE " + requested + " para " + program.name()
                            + ". Procesos activos disponibles: " + available + ".");
        }
        if (activeProcesses.size() > 1) {
            return ResolveResult.error(
                    "AMBIGUOUS_PROCESS",
                    "Hay más de un proceso activo para esa carrera. Contacte soporte técnico.");
        }

        ProcessSummary summary = activeProcesses.getFirst();
        EnrichedProcessDetail detail = getProcessDetailUseCase.getDetail(summary.id(), ctx);
        return ResolveResult.ok(
                new ResolvedProcess(
                        summary.id(),
                        summary.careerName(),
                        summary.careerCode(),
                        summary.templateType(),
                        summary.templateName()
                ),
                detail
        );
    }

    static List<Map<String, Object>> listActiveProcessPayload(String careerQuery,
                                                              String templateType,
                                                              AssistantAuthContext auth,
                                                              ListProcessesUseCase listProcessesUseCase) {
        ProcessQueryContext ctx = new ProcessQueryContext(auth.role(), auth.programScope());
        AssistantProcessQueryParser.ParsedProcessQuery parsed = AssistantProcessQueryParser.parse(
                careerQuery == null ? "" : careerQuery);
        String effectiveCareerQuery = parsed.careerQuery();
        String effectiveTemplateType = AssistantProcessQueryParser.normalizeTemplateType(
                templateType != null ? templateType : parsed.templateType());

        return listProcessesUseCase.list(ctx).stream()
                .filter(summary -> ProcessStatus.ACTIVE.name().equals(summary.status()))
                .filter(summary -> matchesTemplateType(summary, effectiveTemplateType))
                .filter(summary -> matchesCareerQuery(summary, effectiveCareerQuery))
                .sorted(Comparator.comparing(ProcessSummary::careerName))
                .map(AssistantProcessResolver::toProcessMap)
                .toList();
    }

    static List<Map<String, Object>> toLevel1Payload(EnrichedProcessDetail detail) {
        return detail.level1Nodes().stream()
                .sorted(Comparator.comparing(node -> node.getOrder() == null ? Integer.MAX_VALUE : node.getOrder()))
                .map(AssistantProcessResolver::toLevel1Map)
                .toList();
    }

    static List<Map<String, Object>> toNormativeStructurePayload(EnrichedProcessDetail detail) {
        return toLevel1Payload(detail);
    }

    private static Map<String, Object> toLevel1Map(Level1Node level1) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("level1Id", level1.getId().toString());
        map.put("name", level1.getName());
        map.put("order", level1.getOrder());
        map.put("status", level1.getStatus() != null ? level1.getStatus().name() : null);
        List<Map<String, Object>> level2Nodes = level1.getLevel2Nodes() == null
                ? List.of()
                : level1.getLevel2Nodes().stream()
                        .sorted(Comparator.comparing(n -> n.getOrder() == null ? Integer.MAX_VALUE : n.getOrder()))
                        .map(AssistantProcessResolver::toLevel2Map)
                        .toList();
        map.put("level2Nodes", level2Nodes);
        map.put("indicatorCount", countIndicatorsInLevel1(level1));
        return map;
    }

    private static Map<String, Object> toLevel2Map(Level2Node level2) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("level2Id", level2.getId().toString());
        map.put("name", level2.getName());
        map.put("order", level2.getOrder());
        List<Map<String, Object>> level3Nodes = level2.getLevel3Nodes() == null
                ? List.of()
                : level2.getLevel3Nodes().stream()
                        .sorted(Comparator.comparing(n -> n.getOrder() == null ? Integer.MAX_VALUE : n.getOrder()))
                        .map(AssistantProcessResolver::toLevel3Map)
                        .toList();
        map.put("level3Nodes", level3Nodes);
        return map;
    }

    private static Map<String, Object> toLevel3Map(Level3Node level3) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("level3Id", level3.getId().toString());
        map.put("name", level3.getName());
        map.put("order", level3.getOrder());
        List<Map<String, Object>> indicators = level3.getIndicators() == null
                ? List.of()
                : level3.getIndicators().stream()
                        .sorted(Comparator.comparing(i -> i.getOrder() == null ? Integer.MAX_VALUE : i.getOrder()))
                        .map(AssistantProcessResolver::toIndicatorMap)
                        .toList();
        map.put("indicators", indicators);
        return map;
    }

    private static Map<String, Object> toIndicatorMap(NormativeIndicator indicator) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("indicatorId", indicator.getId().toString());
        map.put("code", indicator.getCode());
        map.put("name", indicator.getCode());
        map.put("order", indicator.getOrder());
        map.put("status", indicator.getStatus() != null ? indicator.getStatus().name() : null);
        map.put("referenceUrl", indicator.getReferenceUrl());
        return map;
    }

    private static int countIndicatorsInLevel1(Level1Node level1) {
        if (level1.getLevel2Nodes() == null) {
            return 0;
        }
        return level1.getLevel2Nodes().stream()
                .mapToInt(level2 -> level2.getLevel3Nodes() == null ? 0
                        : level2.getLevel3Nodes().stream()
                                .mapToInt(level3 -> level3.getIndicators() == null ? 0 : level3.getIndicators().size())
                                .sum())
                .sum();
    }

    private static Map<String, Object> toProcessMap(ProcessSummary summary) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("processId", summary.id().toString());
        map.put("careerId", summary.careerId().toString());
        map.put("careerCode", summary.careerCode());
        map.put("careerName", summary.careerName());
        map.put("templateId", summary.templateId().toString());
        map.put("templateName", summary.templateName());
        map.put("templateType", summary.templateType());
        map.put("status", summary.status());
        map.put("startDate", summary.startDate() != null ? summary.startDate().toString() : null);
        map.put("level1Count", summary.level1Count());
        map.put("indicatorCount", summary.indicatorCount());
        if (summary.responsible() != null) {
            map.put("responsibleName", summary.responsible().fullName());
            map.put("responsibleEmail", summary.responsible().email());
        }
        return map;
    }

    private static boolean matchesTemplateType(ProcessSummary summary, String templateType) {
        if (templateType == null || templateType.isBlank()) {
            return true;
        }
        return templateType.equalsIgnoreCase(summary.templateType());
    }

    private static boolean matchesCareerQuery(ProcessSummary summary, String careerQuery) {
        if (careerQuery == null || careerQuery.isBlank()) {
            return true;
        }
        String normalized = normalize(careerQuery);
        return normalize(summary.careerName()).contains(normalized)
                || normalize(summary.careerCode()).contains(normalized);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
