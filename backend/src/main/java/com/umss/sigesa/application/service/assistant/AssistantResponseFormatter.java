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
                if ("CREATE_SUBPHASE".equals(String.valueOf(previewMap.get("requestedAction")))) {
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

        return "Consulta completada.";
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
