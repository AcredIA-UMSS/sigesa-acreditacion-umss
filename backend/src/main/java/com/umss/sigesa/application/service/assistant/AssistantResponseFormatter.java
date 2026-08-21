package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.umss.sigesa.application.model.assistant.ToolExecutionResult;

import java.util.ArrayList;
import java.util.List;

final class AssistantResponseFormatter {

    private AssistantResponseFormatter() {
    }

    static String format(ToolExecutionResult result) {
        if (result == null) {
            return "No pude obtener datos del sistema.";
        }
        if (!result.ok()) {
            return result.error() != null ? result.error().message() : "Operación no completada.";
        }
        if (!(result.data() instanceof java.util.Map<?, ?> dataMap)) {
            return "Operación completada.";
        }

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> data = (java.util.Map<String, Object>) dataMap;

        if (Boolean.TRUE.equals(data.get("confirmationRequired"))) {
            Object message = data.get("message");
            Object preview = data.get("preview");
            StringBuilder sb = new StringBuilder();
            if (message != null) {
                sb.append(message).append("\n\n");
            }
            if (preview instanceof java.util.Map<?, ?> previewMap) {
                Object planSummary = previewMap.get("planSummary");
                if (planSummary != null && !planSummary.toString().isBlank()) {
                    sb.append("Resumen: ").append(planSummary).append("\n\n");
                } else if ("CREATE_SUBPHASE".equals(String.valueOf(previewMap.get("requestedAction")))) {
                    sb.append("Resumen: «").append(previewMap.get("name"))
                            .append("» → orden ").append(previewMap.get("assignedOrder"))
                            .append(" en «").append(previewMap.get("phaseName")).append("».\n\n");
                } else {
                    sb.append("Vista previa: ").append(previewMap).append("\n\n");
                }
            }
            sb.append("Responda **confirmo** para ejecutar la acción.");
            return sb.toString().trim();
        }

        if (Boolean.TRUE.equals(data.get("executed"))) {
            Object message = data.get("message");
            return message != null ? message.toString() : "Acción ejecutada correctamente.";
        }

        if (data.containsKey("processes")) {
            return formatActiveProcesses(data);
        }
        if (data.containsKey("phases")) {
            return formatPhases(data);
        }
        if (data.containsKey("users")) {
            return formatUsers(data);
        }
        if (data.containsKey("email") && data.containsKey("role") && data.containsKey("status")
                && data.containsKey("userId")) {
            return formatUserDetail(data);
        }
        if (data.containsKey("evidences")) {
            return formatPendingEvidences(data);
        }
        if (data.containsKey("evidence") && data.get("evidence") instanceof java.util.Map<?, ?>) {
            return formatEvidenceDetail(data);
        }
        if (data.containsKey("complete") && data.containsKey("hasEvidence")) {
            return formatEvidenceCompleteness(data);
        }
        if (data.containsKey("documents")) {
            return formatNormativeDocuments(data);
        }

        return "Consulta completada.";
    }

    @SuppressWarnings("unchecked")
    static boolean requiresConfirmation(ToolExecutionResult result) {
        if (result == null || !result.ok() || !(result.data() instanceof java.util.Map<?, ?> dataMap)) {
            return false;
        }
        return Boolean.TRUE.equals(((java.util.Map<String, Object>) dataMap).get("confirmationRequired"));
    }

    private static String formatPendingEvidences(java.util.Map<String, Object> data) {
        Object evidencesNode = data.get("evidences");
        if (!(evidencesNode instanceof List<?> evidences) || evidences.isEmpty()) {
            return "No hay evidencias pendientes de revisión (estado SUBIDO) en su alcance.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Evidencias pendientes de revisión (**")
                .append(data.getOrDefault("stateFilter", "SUBIDO"))
                .append("**) — ")
                .append(data.getOrDefault("total", evidences.size()))
                .append(":\n\n");
        int index = 1;
        for (Object item : evidences) {
            if (!(item instanceof java.util.Map<?, ?> map)) {
                continue;
            }
            sb.append(index++).append(". Indicador `").append(shortId(map.get("indicatorId"))).append("`");
            if (map.get("currentState") != null) {
                sb.append(" — estado **").append(map.get("currentState")).append("**");
            }
            sb.append("\n");
            if (map.get("programId") != null) {
                sb.append("   - Programa: `").append(shortId(map.get("programId"))).append("`\n");
            }
            if (map.get("description") != null && !map.get("description").toString().isBlank()) {
                sb.append("   - Descripción: ").append(map.get("description")).append("\n");
            }
            if (map.get("versionNumber") != null) {
                sb.append("   - Versión: ").append(map.get("versionNumber")).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    private static String formatEvidenceDetail(java.util.Map<String, Object> data) {
        Object evidenceNode = data.get("evidence");
        if (!(evidenceNode instanceof java.util.Map<?, ?> map)) {
            return "No se encontró detalle de la evidencia.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Detalle de evidencia\n\n");
        sb.append("- Indicador: `").append(shortId(map.get("indicatorId"))).append("`\n");
        sb.append("- Estado: **").append(map.get("currentState")).append("**\n");
        if (map.get("evidenceId") != null) {
            sb.append("- Evidencia: `").append(shortId(map.get("evidenceId"))).append("`\n");
        }
        if (map.get("versionNumber") != null) {
            sb.append("- Versión: ").append(map.get("versionNumber")).append("\n");
        }
        if (map.get("criterionId") != null) {
            sb.append("- Criterio: `").append(shortId(map.get("criterionId"))).append("`\n");
        }
        if (map.get("description") != null) {
            sb.append("- Descripción: ").append(map.get("description")).append("\n");
        }
        if (map.get("contentHash") != null) {
            sb.append("- SHA-256: `").append(map.get("contentHash")).append("`\n");
        }
        if (map.get("createdAt") != null) {
            sb.append("- Cargada: ").append(map.get("createdAt")).append("\n");
        }
        return sb.toString().trim();
    }

    private static String formatEvidenceCompleteness(java.util.Map<String, Object> data) {
        boolean complete = Boolean.TRUE.equals(data.get("complete"));
        StringBuilder sb = new StringBuilder();
        sb.append(complete
                ? "La evidencia del indicador está **completa**.\n\n"
                : "La evidencia del indicador está **incompleta**.\n\n");
        sb.append("- Indicador: `").append(shortId(data.get("indicatorId"))).append("`\n");
        sb.append("- Estado: ").append(data.get("currentState")).append("\n");
        sb.append("- Tiene archivo/evidencia: ").append(yesNo(data.get("hasEvidence"))).append("\n");
        sb.append("- Tiene descripción: ").append(yesNo(data.get("hasDescription"))).append("\n");
        sb.append("- Tiene criterio: ").append(yesNo(data.get("hasCriterion"))).append("\n");
        sb.append("- Tiene hash de contenido: ").append(yesNo(data.get("hasContentHash"))).append("\n");
        return sb.toString().trim();
    }

    private static String yesNo(Object value) {
        return Boolean.TRUE.equals(value) ? "sí" : "no";
    }

    private static String shortId(Object value) {
        if (value == null) {
            return "—";
        }
        String s = value.toString();
        return s.length() > 8 ? s.substring(0, 8) : s;
    }

    private static String formatActiveProcesses(java.util.Map<String, Object> data) {
        Object processesNode = data.get("processes");
        if (!(processesNode instanceof List<?> processes) || processes.isEmpty()) {
            return "No hay procesos de acreditación activos con los filtros indicados.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Procesos de acreditación **ACTIVOS** (").append(processes.size()).append("):\n\n");
        int index = 1;
        for (Object item : processes) {
            if (!(item instanceof java.util.Map<?, ?> map)) {
                continue;
            }
            sb.append(index++).append(". **").append(map.get("careerName")).append("** (")
                    .append(map.get("careerCode")).append(")\n");
            sb.append("   - Plantilla: ").append(map.get("templateName"))
                    .append(" [").append(map.get("templateType")).append("]\n");
            sb.append("   - Proceso: ").append(map.get("processId")).append("\n");
            sb.append("   - Fases: ").append(map.get("phaseCount"))
                    .append(" | Subfases: ").append(map.get("subphaseCount")).append("\n");
            if (map.get("startDate") != null) {
                sb.append("   - Inicio: ").append(map.get("startDate")).append("\n");
            }
            if (map.get("responsibleName") != null) {
                sb.append("   - Responsable CC: ").append(map.get("responsibleName"))
                        .append(" (").append(map.get("responsibleEmail")).append(")\n");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    private static String formatPhases(java.util.Map<String, Object> data) {
        Object phasesNode = data.get("phases");
        if (!(phasesNode instanceof List<?> phases) || phases.isEmpty()) {
            return "El proceso activo no tiene fases registradas.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Fases del proceso **").append(data.get("careerName")).append("** [")
                .append(data.get("templateType")).append("] (")
                .append(data.get("careerCode")).append("):\n\n");

        for (Object item : phases) {
            if (!(item instanceof java.util.Map<?, ?> map)) {
                continue;
            }
            sb.append(map.get("order")).append(". **").append(map.get("name")).append("**");
            if (map.get("description") != null && !map.get("description").toString().isBlank()) {
                sb.append(" — ").append(map.get("description"));
            }
            sb.append("\n");
            sb.append("   - ID: ").append(map.get("phaseId"));
            Object subphasesNode = map.get("subphases");
            if (subphasesNode instanceof List<?> subphases && !subphases.isEmpty()) {
                sb.append(" | Subfases: ").append(subphases.size()).append("\n");
                for (Object subItem : subphases) {
                    if (!(subItem instanceof java.util.Map<?, ?> subMap)) {
                        continue;
                    }
                    sb.append("     · ").append(subMap.get("order")).append(". ")
                            .append(subMap.get("name"));
                    if (subMap.get("referenceUrl") != null) {
                        sb.append(" → ").append(subMap.get("referenceUrl"));
                    }
                    sb.append("\n");
                }
            } else {
                sb.append(" | Subfases: ").append(map.get("subphaseCount")).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private static String formatUsers(java.util.Map<String, Object> data) {
        Object usersNode = data.get("users");
        if (!(usersNode instanceof List<?> users) || users.isEmpty()) {
            return "No hay usuarios que coincidan con el filtro.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Usuarios (").append(data.get("total")).append("):\n\n");
        for (Object item : users) {
            if (!(item instanceof java.util.Map<?, ?> map)) {
                continue;
            }
            sb.append("- **").append(map.get("fullName")).append("** (")
                    .append(map.get("email")).append(") — ")
                    .append(map.get("role")).append(" / ").append(map.get("status")).append("\n");
        }
        return sb.toString().trim();
    }

    private static String formatUserDetail(java.util.Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("**").append(data.get("fullName")).append("**\n");
        sb.append("- Correo: ").append(data.get("email")).append("\n");
        sb.append("- Rol: ").append(data.get("role")).append("\n");
        sb.append("- Estado: ").append(data.get("status")).append("\n");
        if (data.get("createdAt") != null) {
            sb.append("- Creado: ").append(data.get("createdAt")).append("\n");
        }
        if (data.get("updatedAt") != null) {
            sb.append("- Último cambio: ").append(data.get("updatedAt")).append("\n");
        }
        Object programs = data.get("programs");
        if (programs instanceof List<?> list && !list.isEmpty()) {
            sb.append("- Carreras:\n");
            for (Object item : list) {
                if (item instanceof java.util.Map<?, ?> map) {
                    sb.append("  · ").append(map.get("code")).append(" — ").append(map.get("name")).append("\n");
                }
            }
        } else {
            sb.append("- Carreras: (sin asignación activa)\n");
        }
        return sb.toString().trim();
    }

    private static String formatNormativeDocuments(java.util.Map<String, Object> data) {
        Object documentsNode = data.get("documents");
        Object query = data.get("query");
        if (!(documentsNode instanceof List<?> documents) || documents.isEmpty()) {
            return "No encontré fragmentos normativos para «"
                    + (query != null ? query : "su consulta")
                    + "». Pruebe con términos como CEUB, ARCU-SUR o el nombre de una subfase.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Fragmentos normativos (").append(data.get("total")).append(") para «")
                .append(query).append("»:\n\n");
        int index = 1;
        for (Object item : documents) {
            if (!(item instanceof java.util.Map<?, ?> map)) {
                continue;
            }
            sb.append(index++).append(". **").append(map.get("title")).append("**");
            if (map.get("templateType") != null) {
                sb.append(" (").append(map.get("templateType")).append(')');
            }
            sb.append('\n');
            if (map.get("phaseName") != null && map.get("subphaseName") != null) {
                sb.append("   ").append(map.get("phaseName"))
                        .append(" → ").append(map.get("subphaseName")).append('\n');
            }
            if (map.get("snippet") != null) {
                sb.append("   ").append(map.get("snippet")).append('\n');
            }
            if (map.get("sourceUrl") != null) {
                sb.append("   Enlace: ").append(map.get("sourceUrl")).append('\n');
            }
            sb.append('\n');
        }
        sb.append("Fuente: índice normativo SIGESA (RAG).");
        return sb.toString().trim();
    }

    static ToolExecutionResult parseToolJson(String json, com.fasterxml.jackson.databind.ObjectMapper mapper)
            throws com.fasterxml.jackson.core.JsonProcessingException {
        JsonNode root = mapper.readTree(json);
        boolean ok = root.path("ok").asBoolean(false);
        if (!ok) {
            JsonNode error = root.path("error");
            return ToolExecutionResult.failure(
                    error.path("code").asText("ERROR"),
                    error.path("message").asText("Error desconocido."));
        }
        return ToolExecutionResult.success(mapper.treeToValue(root.path("data"), Object.class));
    }
}
