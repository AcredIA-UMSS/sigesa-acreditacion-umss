package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.model.process.ProcessSummary;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.in.ListProgramsUseCase;
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

    static List<Map<String, Object>> toPhasePayload(EnrichedProcessDetail detail) {
        return detail.phases().stream()
                .sorted(Comparator.comparing(phase -> phase.getOrder() == null ? Integer.MAX_VALUE : phase.getOrder()))
                .map(phase -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("phaseId", phase.getId().toString());
                    map.put("name", phase.getName());
                    map.put("order", phase.getOrder());
                    map.put("description", phase.getDescription());
                    map.put("subphaseCount", phase.getSubphases() == null ? 0 : phase.getSubphases().size());
                    return map;
                })
                .toList();
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
        map.put("phaseCount", summary.phaseCount());
        map.put("subphaseCount", summary.subphaseCount());
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
