package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.ToolExecutionResult;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantResponseFormatterTest {

    @Test
    void format_whenNotOk_includesErrorMessage() {
        ToolExecutionResult result = ToolExecutionResult.failure("ACCESS_DENIED", "No tiene permisos.");

        String formatted = AssistantResponseFormatter.format(result);

        assertThat(formatted).isEqualTo("No tiene permisos.");
    }

    @Test
    void format_whenConfirmationRequired_includesConfirmoPrompt() {
        Map<String, Object> preview = Map.of(
                "requestedAction", "CREATE_SUBPHASE",
                "name", "Evidencia docente",
                "assignedOrder", 2,
                "phaseName", "Fase 2");
        Map<String, Object> data = Map.of(
                "confirmationRequired", true,
                "message", "Vista previa de subfase.",
                "preview", preview);

        String formatted = AssistantResponseFormatter.format(ToolExecutionResult.success(data));

        assertThat(formatted).contains("Vista previa de subfase.");
        assertThat(formatted).contains("Evidencia docente");
        assertThat(formatted).contains("confirmo");
        assertThat(AssistantResponseFormatter.requiresConfirmation(ToolExecutionResult.success(data))).isTrue();
    }

    @Test
    void format_whenExecuted_includesSuccessMessage() {
        Map<String, Object> data = Map.of(
                "executed", true,
                "message", "Subfase «Evidencia docente» creada en «Fase 2» con orden 2.");

        String formatted = AssistantResponseFormatter.format(ToolExecutionResult.success(data));

        assertThat(formatted).contains("creada");
        assertThat(formatted).contains("Evidencia docente");
    }

    @Test
    void format_phasesData_listsPhasesWithCareerHeader() {
        Map<String, Object> phase = new LinkedHashMap<>();
        phase.put("order", 1);
        phase.put("name", "Fase 1");
        phase.put("phaseId", "p1");
        phase.put("subphaseCount", 2);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("careerName", "Ingeniería de Sistemas");
        data.put("careerCode", "INF-SIS");
        data.put("templateType", "CEUB");
        data.put("phases", List.of(phase));

        String formatted = AssistantResponseFormatter.format(ToolExecutionResult.success(data));

        assertThat(formatted).contains("Fases del proceso **Ingeniería de Sistemas**");
        assertThat(formatted).contains("1. **Fase 1**");
        assertThat(formatted).contains("Subfases: 2");
    }

    @Test
    void format_usersData_listsUsersWithTotal() {
        Map<String, Object> user = Map.of(
                "fullName", "Ana Perez",
                "email", "ana@umss.edu.bo",
                "role", "CC",
                "status", "ACTIVE");
        Map<String, Object> data = Map.of(
                "total", 1,
                "users", List.of(user));

        String formatted = AssistantResponseFormatter.format(ToolExecutionResult.success(data));

        assertThat(formatted).contains("Usuarios (1):");
        assertThat(formatted).contains("Ana Perez");
        assertThat(formatted).contains("ana@umss.edu.bo");
    }

    @Test
    void format_pendingEvidencesData_listsPendingItems() {
        UUID indicatorId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Map<String, Object> evidence = Map.of(
                "indicatorId", indicatorId.toString(),
                "currentState", "SUBIDO",
                "description", "Matriz de evidencias");
        Map<String, Object> data = Map.of(
                "total", 1,
                "stateFilter", "SUBIDO",
                "evidences", List.of(evidence));

        String formatted = AssistantResponseFormatter.format(ToolExecutionResult.success(data));

        assertThat(formatted).contains("Evidencias pendientes de revisión");
        assertThat(formatted).contains("123e4567");
        assertThat(formatted).contains("Matriz de evidencias");
    }

    @Test
    void format_normativeDocumentsData_listsDocumentsWithQuery() {
        Map<String, Object> document = Map.of(
                "title", "Matriz CEUB",
                "templateType", "CEUB",
                "snippet", "Requisitos matriz");
        Map<String, Object> data = Map.of(
                "query", "matriz CEUB",
                "total", 1,
                "documents", List.of(document));

        String formatted = AssistantResponseFormatter.format(ToolExecutionResult.success(data));

        assertThat(formatted).contains("Fragmentos normativos (1) para «matriz CEUB»");
        assertThat(formatted).contains("Matriz CEUB");
        assertThat(formatted).contains("Requisitos matriz");
    }

    @Test
    void format_nullOrNonMapData_returnsGenericMessage() {
        assertThat(AssistantResponseFormatter.format(null))
                .isEqualTo("No pude obtener datos del sistema.");
        assertThat(AssistantResponseFormatter.format(ToolExecutionResult.success(null)))
                .isEqualTo("Operación completada.");
        assertThat(AssistantResponseFormatter.format(ToolExecutionResult.success("plain string")))
                .isEqualTo("Operación completada.");
    }
}
