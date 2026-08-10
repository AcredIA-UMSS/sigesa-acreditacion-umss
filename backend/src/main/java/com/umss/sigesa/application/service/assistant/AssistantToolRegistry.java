package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.AssistantToolDefinition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AssistantToolRegistry {

    static final String LIST_USERS_ID = "list_users";
    static final String LIST_PROGRAMS_ID = "list_programs";
    static final String LIST_PROCESS_PHASES_ID = "list_process_phases";
    static final String SET_USER_STATUS_ID = "set_user_status";
    static final String LIST_ACTIVE_PROCESSES_ID = "list_active_processes";
    static final String MANAGE_PROCESS_PHASE_ID = "manage_process_phase";

    private static final Set<String> JD_ONLY = Set.of("JD");
    private static final Set<String> JD_AND_TD = Set.of("JD", "TD");

    private static final AssistantToolDefinition LIST_USERS = new AssistantToolDefinition(
            LIST_USERS_ID,
            "Lista usuarios SIGESA (correo, nombre, rol, estado). Solo JD. Filtros opcionales role/status.",
            JD_ONLY,
            "read",
            listUsersParameterSchema()
    );

    private static final AssistantToolDefinition LIST_PROGRAMS = new AssistantToolDefinition(
            LIST_PROGRAMS_ID,
            "Lista carreras/programas académicos con código y nombre. JD y TD. "
                    + "Usa query opcional para buscar por nombre o código.",
            JD_AND_TD,
            "read",
            listProgramsParameterSchema()
    );

    private static final AssistantToolDefinition LIST_PROCESS_PHASES = new AssistantToolDefinition(
            LIST_PROCESS_PHASES_ID,
            "Lista las fases del proceso de acreditación ACTIVO de una carrera, ordenadas por campo order. "
                    + "JD y TD. Indica careerQuery (nombre o código). Opcional templateType: CEUB o ARCU-SUR.",
            JD_AND_TD,
            "read",
            listProcessPhasesParameterSchema()
    );

    private static final AssistantToolDefinition LIST_ACTIVE_PROCESSES = new AssistantToolDefinition(
            LIST_ACTIVE_PROCESSES_ID,
            "Lista carreras con procesos de acreditación ACTIVE y sus detalles (plantilla, conteos, responsable). "
                    + "JD y TD. Filtros opcionales careerQuery y templateType (CEUB o ARCU-SUR).",
            JD_AND_TD,
            "read",
            listActiveProcessesParameterSchema()
    );

    private static final AssistantToolDefinition SET_USER_STATUS = new AssistantToolDefinition(
            SET_USER_STATUS_ID,
            "Activa o desactiva un usuario por correo institucional o nombre. Solo JD. "
                    + "Requiere action ACTIVATE o DEACTIVATE. "
                    + "Primero invoca con confirmed=false para obtener vista previa; "
                    + "solo ejecuta con confirmed=true tras confirmación explícita del usuario en el chat.",
            JD_ONLY,
            "write",
            setUserStatusParameterSchema()
    );

    private static final AssistantToolDefinition MANAGE_PROCESS_PHASE = new AssistantToolDefinition(
            MANAGE_PROCESS_PHASE_ID,
            "Crea, edita, elimina u ordena fases del proceso ACTIVO de una carrera. JD y TD. "
                    + "Acciones: CREATE, UPDATE, DELETE, REORDER. "
                    + "Primero invoca con confirmed=false para vista previa; "
                    + "solo ejecuta con confirmed=true tras confirmación explícita del usuario en el chat.",
            JD_AND_TD,
            "write",
            manageProcessPhaseParameterSchema()
    );

    private final List<AssistantToolDefinition> allTools = List.of(
            LIST_USERS,
            LIST_PROGRAMS,
            LIST_ACTIVE_PROCESSES,
            LIST_PROCESS_PHASES,
            SET_USER_STATUS,
            MANAGE_PROCESS_PHASE
    );

    public List<AssistantToolDefinition> toolsForRole(String role) {
        if (role == null || role.isBlank()) {
            return List.of();
        }
        String normalizedRole = role.trim().toUpperCase();
        return allTools.stream()
                .filter(tool -> tool.allowedRoles().contains(normalizedRole))
                .toList();
    }

    public Optional<AssistantToolDefinition> findById(String toolId) {
        if (toolId == null || toolId.isBlank()) {
            return Optional.empty();
        }
        return allTools.stream()
                .filter(tool -> tool.id().equals(toolId))
                .findFirst();
    }

    private static Map<String, Object> listUsersParameterSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("role", enumProperty(List.of("JD", "CC", "TD", "EE"), "Filtro opcional por rol de usuario."));
        properties.put("status", enumProperty(
                List.of("INACTIVE", "ACTIVE", "DEACTIVATED"),
                "Filtro opcional por estado de cuenta."));
        return objectSchema(properties);
    }

    private static Map<String, Object> listProgramsParameterSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("query", stringProperty("Búsqueda opcional por nombre o código de carrera."));
        return objectSchema(properties);
    }

    private static Map<String, Object> listProcessPhasesParameterSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        Map<String, Object> careerQuery = stringProperty("Nombre o código de la carrera.");
        careerQuery.put("minLength", 2);
        properties.put("careerQuery", careerQuery);
        properties.put("templateType", enumProperty(
                List.of("CEUB", "ARCU-SUR"),
                "Opcional. Tipo de plantilla del proceso (CEUB o ARCU-SUR)."));
        return requiredObjectSchema(properties, List.of("careerQuery"));
    }

    private static Map<String, Object> listActiveProcessesParameterSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("careerQuery", stringProperty("Filtro opcional por nombre o código de carrera."));
        properties.put("templateType", enumProperty(
                List.of("CEUB", "ARCU-SUR"),
                "Filtro opcional por tipo de plantilla."));
        return objectSchema(properties);
    }

    private static Map<String, Object> setUserStatusParameterSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("identifier", stringProperty("Correo @umss.edu.bo o nombre del usuario."));
        properties.put("action", enumProperty(List.of("ACTIVATE", "DEACTIVATE"), "Acción a realizar."));
        properties.put("confirmed", booleanProperty(
                "false para vista previa; true solo tras confirmación explícita del usuario."));
        return requiredObjectSchema(properties, List.of("identifier", "action"));
    }

    private static Map<String, Object> manageProcessPhaseParameterSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("action", enumProperty(
                List.of("CREATE", "UPDATE", "DELETE", "REORDER"),
                "Operación sobre fases."));
        properties.put("careerQuery", stringProperty("Nombre o código de la carrera."));
        properties.put("phaseId", stringProperty("UUID de la fase (UPDATE/DELETE)."));
        properties.put("phaseName", stringProperty("Nombre de la fase (alternativa a phaseId)."));
        properties.put("name", stringProperty("Nombre de la fase (CREATE/UPDATE)."));
        properties.put("order", integerProperty("Orden de la fase (CREATE/UPDATE)."));
        properties.put("description", stringProperty("Descripción opcional (CREATE/UPDATE)."));
        properties.put("phaseIds", arrayProperty("Lista ordenada de UUID de fases (REORDER)."));
        properties.put("confirmed", booleanProperty(
                "false para vista previa; true solo tras confirmación explícita del usuario."));
        return requiredObjectSchema(properties, List.of("action", "careerQuery"));
    }

    private static Map<String, Object> objectSchema(Map<String, Object> properties) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    private static Map<String, Object> requiredObjectSchema(Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = objectSchema(properties);
        schema.put("required", required);
        return schema;
    }

    private static Map<String, Object> stringProperty(String description) {
        Map<String, Object> property = new LinkedHashMap<>();
        property.put("type", "string");
        property.put("description", description);
        return property;
    }

    private static Map<String, Object> integerProperty(String description) {
        Map<String, Object> property = new LinkedHashMap<>();
        property.put("type", "integer");
        property.put("description", description);
        return property;
    }

    private static Map<String, Object> booleanProperty(String description) {
        Map<String, Object> property = new LinkedHashMap<>();
        property.put("type", "boolean");
        property.put("description", description);
        return property;
    }

    private static Map<String, Object> enumProperty(List<String> values, String description) {
        Map<String, Object> property = new LinkedHashMap<>();
        property.put("type", "string");
        property.put("enum", values);
        property.put("description", description);
        return property;
    }

    private static Map<String, Object> arrayProperty(String description) {
        Map<String, Object> items = new LinkedHashMap<>();
        items.put("type", "string");
        Map<String, Object> property = new LinkedHashMap<>();
        property.put("type", "array");
        property.put("items", items);
        property.put("description", description);
        return property;
    }
}
