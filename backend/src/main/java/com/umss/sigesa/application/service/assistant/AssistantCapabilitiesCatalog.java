package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.AssistantAgentProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AssistantCapabilitiesCatalog {

    private AssistantCapabilitiesCatalog() {
    }

    public static List<String> capabilitiesForRole(String role) {
        return capabilitiesForRoleAndAgent(role, AssistantAgentProfile.GENERAL);
    }

    public static List<String> capabilitiesForRoleAndAgent(String role, AssistantAgentProfile agentProfile) {
        if (role == null || role.isBlank()) {
            return List.of("Inicie sesión para ver las capacidades del asistente.");
        }

        if (agentProfile == AssistantAgentProfile.PHASES) {
            return capabilitiesForPhasesAgent(role);
        }

        List<String> items = new ArrayList<>();
        String normalized = role.trim().toUpperCase(Locale.ROOT);

        if ("JD".equals(normalized) || "TD".equals(normalized)) {
            items.add("Listar fases del proceso activo de una carrera (palabra clave: «fases»).");
            items.add("Listar carreras con proceso de acreditación activo.");
            items.add("Listar carreras/programas académicos.");
            items.add("Gestionar fases del proceso activo (con confirmación en chat).");
        }
        if ("JD".equals(normalized)) {
            items.add("Listar usuarios registrados (palabra clave: «usuarios»).");
            items.add("Activar o desactivar usuarios (con confirmación en chat).");
        }
        if (items.isEmpty()) {
            items.add("Consultas operativas sobre acreditación sin tools administrativas para su rol.");
        }
        return items;
    }

    public static List<String> capabilitiesForPhasesAgent(String role) {
        String normalized = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        List<String> items = new ArrayList<>();
        items.add("Listar fases de este proceso (palabra clave: «fases» o «etapas»).");
        items.add("Ver estructura completa con subfases y enlaces normativos.");
        if ("JD".equals(normalized) || "TD".equals(normalized)) {
            items.add("Crear, renombrar, eliminar u ordenar fases (con confirmación en chat).");
            items.add("Crear, editar o eliminar subfases (con confirmación en chat).");
        }
        if ("CC".equals(normalized)) {
            items.add("Modo solo lectura: no puede modificar la estructura del proceso.");
        }
        items.add("Preguntas sobre el proceso que está viendo en pantalla.");
        return items;
    }

    public static String formatOutOfScopeMessage(String role, boolean llmDisabledWithoutKeyword) {
        return formatOutOfScopeMessage(role, llmDisabledWithoutKeyword, AssistantAgentProfile.GENERAL);
    }

    public static String formatOutOfScopeMessage(String role,
                                                   boolean llmDisabledWithoutKeyword,
                                                   AssistantAgentProfile agentProfile) {
        StringBuilder sb = new StringBuilder();
        sb.append("No puedo responder eso con las herramientas disponibles en SIGESA.\n\n");
        if (llmDisabledWithoutKeyword) {
            sb.append("La consulta no coincide con el catálogo de palabras clave y la IA está desactivada ")
                    .append("(SIGESA_ASSISTANT_LLM_ENABLED=false). ")
                    .append("Use frases del catálogo o active la IA para preguntas con sinónimos.\n\n");
        }
        if (agentProfile == AssistantAgentProfile.PHASES) {
            sb.append("Este copiloto solo cubre fases del proceso actual.\n\n");
        }
        sb.append("Puedo ayudarte con:\n");
        for (String capability : capabilitiesForRoleAndAgent(role, agentProfile)) {
            sb.append("• ").append(capability).append('\n');
        }
        return sb.toString().trim();
    }
}
