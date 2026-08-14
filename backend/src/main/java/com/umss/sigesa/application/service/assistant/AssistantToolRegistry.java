package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.AssistantAgentProfile;
import com.umss.sigesa.application.model.assistant.AssistantToolDefinition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AssistantToolRegistry {

    static final String LIST_USERS_ID = "list_users";
    static final String GET_USER_DETAIL_ID = "get_user_detail";
    static final String CREATE_USER_ID = "create_user";
    static final String LIST_PROGRAMS_ID = "list_programs";
    static final String LIST_PROCESS_PHASES_ID = "list_process_phases";
    static final String LIST_PROCESS_STRUCTURE_ID = "list_process_structure";
    static final String SET_USER_STATUS_ID = "set_user_status";
    static final String MANAGE_USER_STATUS_ID = "manage_user_status";
    static final String MANAGE_USER_ASSIGNMENT_ID = "manage_user_assignment";
    static final String LIST_ACTIVE_PROCESSES_ID = "list_active_processes";
    static final String MANAGE_PROCESS_PHASE_ID = "manage_process_phase";
    static final String MANAGE_PROCESS_SUBPHASE_ID = "manage_process_subphase";
    static final String LIST_PENDING_EVIDENCES_ID = "list_pending_evidences";
    static final String GET_EVIDENCE_DETAIL_ID = "get_evidence_detail";
    static final String CHECK_EVIDENCE_COMPLETENESS_ID = "check_evidence_completeness";

    private static final Set<String> JD_ONLY = Set.of("JD");
    private static final Set<String> JD_AND_TD = Set.of("JD", "TD");
    private static final Set<String> JD_TD_AND_CC = Set.of("JD", "TD", "CC");

    private static final AssistantToolDefinition LIST_USERS = new AssistantToolDefinition(
            LIST_USERS_ID,
            "Lista usuarios SIGESA (correo, nombre, rol, estado). Solo JD. "
                    + "Filtros opcionales role, status y programId (UUID de carrera).",
            JD_ONLY,
            "read",
            listUsersParameterSchema()
    );

    private static final AssistantToolDefinition GET_USER_DETAIL = new AssistantToolDefinition(
            GET_USER_DETAIL_ID,
            "Obtiene el detalle de un usuario por correo o nombre. Solo JD. "
                    + "Incluye rol, estado, programas asignados, createdAt y updatedAt.",
            JD_ONLY,
            "read",
            getUserDetailParameterSchema()
    );

    private static final AssistantToolDefinition CREATE_USER = new AssistantToolDefinition(
            CREATE_USER_ID,
            "Registra un usuario institucional. Solo JD. Requiere email @umss.edu.bo, firstName, lastName, "
                    + "phoneNumber, role (JD/CC/TD/EE). programId obligatorio para CC/EE. "
                    + "La cuenta queda INACTIVE hasta el primer acceso. "
                    + "Primero confirmed=false (vista previa); confirmed=true tras confirmación explícita.",
            JD_ONLY,
            "write",
            createUserParameterSchema()
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
                    + "JD, TD y CC (solo lectura). Indica careerQuery (nombre o código). "
                    + "Opcional templateType: CEUB o ARCU-SUR.",
            JD_TD_AND_CC,
            "read",
            listProcessPhasesParameterSchema()
    );

    private static final AssistantToolDefinition LIST_PROCESS_STRUCTURE = new AssistantToolDefinition(
            LIST_PROCESS_STRUCTURE_ID,
            "Lista el árbol completo Fase → Subfase del proceso ACTIVO, incluyendo referenceUrl. "
                    + "JD, TD y CC (solo lectura). Indica careerQuery (nombre o código). "
                    + "Opcional templateType: CEUB o ARCU-SUR.",
            JD_TD_AND_CC,
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

    private static final AssistantToolDefinition MANAGE_USER_STATUS = new AssistantToolDefinition(
            MANAGE_USER_STATUS_ID,
            "Activa, desactiva o reactiva un usuario (agente users). Solo JD. "
                    + "Acciones: ACTIVATE, DEACTIVATE, REACTIVATE. "
                    + "Primero confirmed=false para vista previa; confirmed=true tras confirmación. "
                    + "Desactivación: el usuario no puede iniciar sesión; historial de auditoría se conserva.",
            JD_ONLY,
            "write",
            manageUserStatusParameterSchema()
    );

    private static final AssistantToolDefinition MANAGE_USER_ASSIGNMENT = new AssistantToolDefinition(
            MANAGE_USER_ASSIGNMENT_ID,
            "Crea o actualiza la asignación user_program_assignment de un usuario CC/EE. Solo JD. "
                    + "Acciones: CREATE, UPDATE. Requiere identifier (correo/nombre) y programId. "
                    + "Aplica mínimo privilegio (una carrera activa). "
                    + "Primero confirmed=false; confirmed=true tras confirmación.",
            JD_ONLY,
            "write",
            manageUserAssignmentParameterSchema()
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

    private static final AssistantToolDefinition MANAGE_PROCESS_SUBPHASE = new AssistantToolDefinition(
            MANAGE_PROCESS_SUBPHASE_ID,
            "Crea, edita o elimina subfases de una fase en el proceso ACTIVO. JD y TD. "
                    + "Acciones: CREATE, UPDATE, DELETE. Requiere referenceUrl HTTPS en CREATE/UPDATE. "
                    + "Indique la fase con phaseOrder (preferido), phaseName o phaseId UUID real (nunca placeholders). "
                    + "Primero invoca con confirmed=false; solo ejecuta con confirmed=true tras confirmación.",
            JD_AND_TD,
            "write",
            manageProcessSubphaseParameterSchema()
    );

    private static final AssistantToolDefinition LIST_PENDING_EVIDENCES = new AssistantToolDefinition(
            LIST_PENDING_EVIDENCES_ID,
            "Lista indicadores con documentación en estado SUBIDO (pendientes de control). "
                    + "JD, TD y CC (CC solo su programScope). programId opcional para acotar por carrera.",
            JD_TD_AND_CC,
            "read",
            listPendingEvidencesParameterSchema()
    );

    private static final AssistantToolDefinition GET_EVIDENCE_DETAIL = new AssistantToolDefinition(
            GET_EVIDENCE_DETAIL_ID,
            "Obtiene metadatos de la evidencia/versión de un indicador (hash, descripción, criterio, estado). "
                    + "JD, TD y CC (CC solo su programScope). Requiere indicatorId.",
            JD_TD_AND_CC,
            "read",
            evidenceIndicatorParameterSchema()
    );

    private static final AssistantToolDefinition CHECK_EVIDENCE_COMPLETENESS = new AssistantToolDefinition(
            CHECK_EVIDENCE_COMPLETENESS_ID,
            "Evalúa checklist de completitud de evidencia (archivo, descripción, criterio, hash, estado). "
                    + "JD, TD y CC (CC solo su programScope). Requiere indicatorId.",
            JD_TD_AND_CC,
            "read",
            evidenceIndicatorParameterSchema()
    );

    private final List<AssistantToolDefinition> allTools = List.of(
            LIST_USERS,
            GET_USER_DETAIL,
            CREATE_USER,
            LIST_PROGRAMS,
            LIST_ACTIVE_PROCESSES,
            LIST_PROCESS_PHASES,
            LIST_PROCESS_STRUCTURE,
            SET_USER_STATUS,
            MANAGE_USER_STATUS,
            MANAGE_USER_ASSIGNMENT,
            MANAGE_PROCESS_PHASE,
            MANAGE_PROCESS_SUBPHASE,
            LIST_PENDING_EVIDENCES,
            GET_EVIDENCE_DETAIL,
            CHECK_EVIDENCE_COMPLETENESS
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

    public List<AssistantToolDefinition> toolsForRoleAndAgent(String role, AssistantAgentProfile agentProfile) {
        List<AssistantToolDefinition> roleTools = toolsForRole(role);
        if (agentProfile == AssistantAgentProfile.PHASES) {
            return roleTools.stream()
                    .filter(tool -> PHASES_AGENT_TOOL_IDS.contains(tool.id()))
                    .toList();
        }
        if (agentProfile == AssistantAgentProfile.USERS) {
            return roleTools.stream()
                    .filter(tool -> USERS_AGENT_TOOL_IDS.contains(tool.id()))
                    .toList();
        }
        if (agentProfile == AssistantAgentProfile.EVIDENCE) {
            return roleTools.stream()
                    .filter(tool -> EVIDENCE_AGENT_TOOL_IDS.contains(tool.id()))
                    .toList();
        }
        return roleTools;
    }

    private static final Set<String> PHASES_AGENT_TOOL_IDS = Set.of(
            LIST_PROCESS_PHASES_ID,
            LIST_PROCESS_STRUCTURE_ID,
            MANAGE_PROCESS_PHASE_ID,
            MANAGE_PROCESS_SUBPHASE_ID
    );

    private static final Set<String> USERS_AGENT_TOOL_IDS = Set.of(
            LIST_USERS_ID,
            GET_USER_DETAIL_ID,
            CREATE_USER_ID,
            MANAGE_USER_STATUS_ID,
            MANAGE_USER_ASSIGNMENT_ID
    );

    private static final Set<String> EVIDENCE_AGENT_TOOL_IDS = Set.of(
            LIST_PENDING_EVIDENCES_ID,
            GET_EVIDENCE_DETAIL_ID,
            CHECK_EVIDENCE_COMPLETENESS_ID
    );

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
        properties.put("programId", stringProperty("Filtro opcional por UUID de carrera/programa."));
        return objectSchema(properties);
    }

    private static Map<String, Object> getUserDetailParameterSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("identifier", stringProperty("Correo @umss.edu.bo o nombre del usuario."));
        return requiredObjectSchema(properties, List.of("identifier"));
    }

    private static Map<String, Object> createUserParameterSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("email", stringProperty("Correo institucional @umss.edu.bo."));
        properties.put("firstName", stringProperty("Nombre(s)."));
        properties.put("lastName", stringProperty("Apellido(s)."));
        properties.put("phoneNumber", stringProperty("Celular boliviano 8 dígitos (6xxxxxxx/7xxxxxxx)."));
        properties.put("role", enumProperty(List.of("JD", "CC", "TD", "EE"), "Rol a asignar."));
        properties.put("programId", stringProperty("UUID de carrera (obligatorio para CC/EE)."));
        properties.put("programQuery", stringProperty("Nombre o código de carrera (alternativa a programId)."));
        properties.put("confirmed", booleanProperty(
                "false para vista previa; true solo tras confirmación explícita del usuario."));
        return requiredObjectSchema(properties, List.of("email", "firstName", "lastName", "phoneNumber", "role"));
    }

    private static Map<String, Object> manageUserStatusParameterSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("identifier", stringProperty("Correo @umss.edu.bo o nombre del usuario."));
        properties.put("action", enumProperty(
                List.of("ACTIVATE", "DEACTIVATE", "REACTIVATE"),
                "Acción a realizar."));
        properties.put("confirmed", booleanProperty(
                "false para vista previa; true solo tras confirmación explícita del usuario."));
        return requiredObjectSchema(properties, List.of("identifier", "action"));
    }

    private static Map<String, Object> manageUserAssignmentParameterSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("identifier", stringProperty("Correo @umss.edu.bo o nombre del usuario CC/EE."));
        properties.put("action", enumProperty(List.of("CREATE", "UPDATE"), "Operación sobre la asignación."));
        properties.put("programId", stringProperty("UUID de la carrera a asignar."));
        properties.put("programQuery", stringProperty("Nombre o código de carrera (alternativa a programId)."));
        properties.put("confirmed", booleanProperty(
                "false para vista previa; true solo tras confirmación explícita del usuario."));
        return requiredObjectSchema(properties, List.of("identifier", "action"));
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
        properties.put("phaseId", stringProperty("UUID real de la fase (obtener de list_process_structure)."));
        properties.put("phaseOrder", integerProperty("Orden numérico de la fase (1, 2, …). Preferido frente a phaseId."));
        properties.put("phaseName", stringProperty("Nombre exacto de la fase (alternativa a phaseOrder/phaseId)."));
        properties.put("name", stringProperty("Nombre de la fase (CREATE/UPDATE)."));
        properties.put("order", integerProperty("Orden de la fase (CREATE/UPDATE)."));
        properties.put("description", stringProperty("Descripción opcional (CREATE/UPDATE)."));
        properties.put("phaseIds", arrayProperty("Lista ordenada de UUID de fases (REORDER)."));
        properties.put("confirmed", booleanProperty(
                "false para vista previa; true solo tras confirmación explícita del usuario."));
        return requiredObjectSchema(properties, List.of("action", "careerQuery"));
    }

    private static Map<String, Object> manageProcessSubphaseParameterSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("action", enumProperty(
                List.of("CREATE", "UPDATE", "DELETE"),
                "Operación sobre subfases."));
        properties.put("careerQuery", stringProperty("Nombre o código de la carrera."));
        properties.put("phaseId", stringProperty("UUID real de la fase contenedora."));
        properties.put("phaseOrder", integerProperty("Orden numérico de la fase (1, 2, …). Preferido frente a phaseId."));
        properties.put("phaseName", stringProperty("Nombre exacto de la fase (alternativa a phaseOrder/phaseId)."));
        properties.put("subphaseId", stringProperty("UUID de la subfase (UPDATE/DELETE)."));
        properties.put("subphaseName", stringProperty("Nombre de la subfase (alternativa a subphaseId)."));
        properties.put("name", stringProperty("Nombre de la subfase (CREATE/UPDATE)."));
        properties.put("order", integerProperty("Orden de la subfase (CREATE/UPDATE)."));
        properties.put("referenceUrl", stringProperty("Enlace HTTPS normativo (CREATE/UPDATE)."));
        properties.put("description", stringProperty("Descripción opcional (CREATE/UPDATE)."));
        properties.put("confirmed", booleanProperty(
                "false para vista previa; true solo tras confirmación explícita del usuario."));
        return requiredObjectSchema(properties, List.of("action", "careerQuery"));
    }

    private static Map<String, Object> listPendingEvidencesParameterSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("programId", stringProperty("UUID opcional de carrera/programa para acotar la consulta."));
        return objectSchema(properties);
    }

    private static Map<String, Object> evidenceIndicatorParameterSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("indicatorId", stringProperty("UUID del indicador a consultar."));
        return requiredObjectSchema(properties, List.of("indicatorId"));
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
