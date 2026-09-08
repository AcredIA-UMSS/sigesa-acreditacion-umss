package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.evidence.EvidenceControlItem;
import com.umss.sigesa.application.port.in.ActivateUserUseCase;
import com.umss.sigesa.application.port.in.CheckEvidenceCompletenessUseCase;
import com.umss.sigesa.application.port.in.DeactivateUserUseCase;
import com.umss.sigesa.application.port.in.GetEvidenceDetailUseCase;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.ListPendingEvidencesUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.in.ListProgramsUseCase;
import com.umss.sigesa.application.port.in.ListUsersUseCase;
import com.umss.sigesa.application.port.in.ManageUserProgramAssignmentUseCase;
import com.umss.sigesa.application.port.in.RegisterUserUseCase;
import com.umss.sigesa.application.port.in.SearchNormativeDocumentsUseCase;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.application.service.assistant.support.AssistantToolExecutorTestFactory;
import com.umss.sigesa.application.service.assistant.support.RecordingAssistantToolAuditPort;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.model.IndicatorState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistantToolExecutorTest {

    @Mock
    private ListUsersUseCase listUsersUseCase;
    @Mock
    private ActivateUserUseCase activateUserUseCase;
    @Mock
    private DeactivateUserUseCase deactivateUserUseCase;
    @Mock
    private RegisterUserUseCase registerUserUseCase;
    @Mock
    private ManageUserProgramAssignmentUseCase manageUserProgramAssignmentUseCase;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private ListProgramsUseCase listProgramsUseCase;
    @Mock
    private ListProcessesUseCase listProcessesUseCase;
    @Mock
    private GetProcessDetailUseCase getProcessDetailUseCase;
    @Mock
    private ListPendingEvidencesUseCase listPendingEvidencesUseCase;
    @Mock
    private GetEvidenceDetailUseCase getEvidenceDetailUseCase;
    @Mock
    private CheckEvidenceCompletenessUseCase checkEvidenceCompletenessUseCase;
    @Mock
    private SearchNormativeDocumentsUseCase searchNormativeDocumentsUseCase;

    private AssistantToolExecutor executor;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RecordingAssistantToolAuditPort auditPort = new RecordingAssistantToolAuditPort();

    @BeforeEach
    void setUp() {
        executor = AssistantToolExecutorTestFactory.createFull(
                new AssistantToolRegistry(),
                listUsersUseCase,
                activateUserUseCase,
                deactivateUserUseCase,
                registerUserUseCase,
                manageUserProgramAssignmentUseCase,
                userRepositoryPort,
                listProgramsUseCase,
                listProcessesUseCase,
                getProcessDetailUseCase,
                listPendingEvidencesUseCase,
                getEvidenceDetailUseCase,
                checkEvidenceCompletenessUseCase,
                searchNormativeDocumentsUseCase,
                auditPort
        );
    }

    @Test
    void executeListUsers_withJdReturnsUsers() throws Exception {
        UUID userId = UUID.randomUUID();
        when(listUsersUseCase.list(isNull(), isNull())).thenReturn(List.of(
                new ListUsersUseCase.UserSummary(
                        userId,
                        "cc@umss.edu.bo",
                        "CC",
                        "ACTIVE",
                        List.of(UUID.randomUUID()),
                        "Demo",
                        "CC",
                        "Demo CC",
                        "71234567"
                )
        ));

        String json = executor.execute(AssistantToolRegistry.LIST_USERS_ID, "{}", jdContext());
        JsonNode root = objectMapper.readTree(json);

        assertThat(root.path("ok").asBoolean()).isTrue();
        assertThat(root.path("data").path("total").asInt()).isEqualTo(1);
        assertThat(root.path("data").path("users").get(0).path("email").asText())
                .isEqualTo("cc@umss.edu.bo");
    }

    @Test
    void executeListUsers_withCcReturnsAccessDenied() throws Exception {
        String json = executor.execute(AssistantToolRegistry.LIST_USERS_ID, "{}", ccContext());
        JsonNode root = objectMapper.readTree(json);

        assertThat(root.path("ok").asBoolean()).isFalse();
        assertThat(root.path("error").path("code").asText()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void executeListUsers_withInvalidRoleReturnsError() throws Exception {
        when(listUsersUseCase.list(any(), any())).thenThrow(new InvalidRoleException("ADMIN"));

        String json = executor.execute(AssistantToolRegistry.LIST_USERS_ID, "{\"role\":\"ADMIN\"}", jdContext());
        JsonNode root = objectMapper.readTree(json);

        assertThat(root.path("ok").asBoolean()).isFalse();
        assertThat(root.path("error").path("code").asText()).isEqualTo("INVALID_ROLE");
    }

    @Test
    void executeSetUserStatus_withoutConfirmationReturnsPreview() throws Exception {
        UUID targetId = UUID.randomUUID();
        UUID jdId = UUID.randomUUID();
        when(listUsersUseCase.list(isNull(), isNull())).thenReturn(List.of(
                new ListUsersUseCase.UserSummary(
                        targetId,
                        "cc@umss.edu.bo",
                        "CC",
                        "ACTIVE",
                        List.of(),
                        "Ana",
                        "Perez",
                        "Ana Perez",
                        null
                )
        ));

        String json = executor.execute(
                AssistantToolRegistry.SET_USER_STATUS_ID,
                "{\"identifier\":\"cc@umss.edu.bo\",\"action\":\"DEACTIVATE\",\"confirmed\":false}",
                new AssistantAuthContext(jdId, "JD", List.of())
        );
        JsonNode root = objectMapper.readTree(json);

        assertThat(root.path("ok").asBoolean()).isTrue();
        assertThat(root.path("data").path("confirmationRequired").asBoolean()).isTrue();
        verify(deactivateUserUseCase, never()).deactivate(any());
    }

    @Test
    void executeSetUserStatus_withConfirmationDeactivatesUser() throws Exception {
        UUID targetId = UUID.randomUUID();
        UUID jdId = UUID.randomUUID();
        when(listUsersUseCase.list(isNull(), isNull())).thenReturn(List.of(
                new ListUsersUseCase.UserSummary(
                        targetId,
                        "cc@umss.edu.bo",
                        "CC",
                        "ACTIVE",
                        List.of(),
                        "Ana",
                        "Perez",
                        "Ana Perez",
                        null
                )
        ));

        String json = executor.execute(
                AssistantToolRegistry.SET_USER_STATUS_ID,
                "{\"identifier\":\"cc@umss.edu.bo\",\"action\":\"DEACTIVATE\",\"confirmed\":true}",
                new AssistantAuthContext(jdId, "JD", List.of())
        );
        JsonNode root = objectMapper.readTree(json);

        assertThat(root.path("ok").asBoolean()).isTrue();
        assertThat(root.path("data").path("executed").asBoolean()).isTrue();
        verify(deactivateUserUseCase).deactivate(eq(targetId));
    }

    @Test
    void execute_unknownToolReturnsNotFound() throws Exception {
        String json = executor.execute("unknown_tool", "{}", jdContext());
        JsonNode root = objectMapper.readTree(json);

        assertThat(root.path("ok").asBoolean()).isFalse();
        assertThat(root.path("error").path("code").asText()).isEqualTo("TOOL_NOT_FOUND");
    }

    @Test
    void executeListPendingEvidences_withTdReturnsEvidences() throws Exception {
        UUID programId = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();
        when(listPendingEvidencesUseCase.list(any(), isNull())).thenReturn(List.of(
                new EvidenceControlItem(
                        indicatorId,
                        programId,
                        null,
                        null,
                        IndicatorState.SUBIDO,
                        UUID.randomUUID(),
                        1,
                        "abc123",
                        "Matriz de evidencias",
                        LocalDateTime.parse("2026-01-15T10:00:00")
                )
        ));

        String json = executor.execute(AssistantToolRegistry.LIST_PENDING_EVIDENCES_ID, "{}", tdContext());
        JsonNode root = objectMapper.readTree(json);

        assertThat(root.path("ok").asBoolean()).isTrue();
        assertThat(root.path("data").path("total").asInt()).isEqualTo(1);
        assertThat(root.path("data").path("evidences").get(0).path("indicatorId").asText())
                .isEqualTo(indicatorId.toString());
        assertThat(root.path("data").path("stateFilter").asText()).isEqualTo("SUBIDO");
    }

    @Test
    void executeListPendingEvidences_withCcScopedProgram() throws Exception {
        UUID programId = UUID.randomUUID();
        AssistantAuthContext auth = ccContext();
        when(listPendingEvidencesUseCase.list(eq(auth), eq(programId))).thenReturn(List.of(
                new EvidenceControlItem(
                        UUID.randomUUID(),
                        programId,
                        null,
                        null,
                        IndicatorState.SUBIDO,
                        UUID.randomUUID(),
                        1,
                        null,
                        "Evidencia CC",
                        null
                )
        ));

        String json = executor.execute(
                AssistantToolRegistry.LIST_PENDING_EVIDENCES_ID,
                "{\"programId\":\"" + programId + "\"}",
                auth);
        JsonNode root = objectMapper.readTree(json);

        assertThat(root.path("ok").asBoolean()).isTrue();
        assertThat(root.path("data").path("total").asInt()).isEqualTo(1);
        verify(listPendingEvidencesUseCase).list(eq(auth), eq(programId));
    }

    @Test
    void executeGetEvidenceDetail_withValidIndicator() throws Exception {
        UUID indicatorId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID evidenceId = UUID.randomUUID();
        when(getEvidenceDetailUseCase.get(any(), eq(indicatorId))).thenReturn(Optional.of(
                new EvidenceControlItem(
                        indicatorId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        IndicatorState.SUBIDO,
                        evidenceId,
                        2,
                        "deadbeef",
                        "Detalle evidencia",
                        LocalDateTime.parse("2026-02-01T12:00:00")
                )
        ));

        String json = executor.execute(
                AssistantToolRegistry.GET_EVIDENCE_DETAIL_ID,
                "{\"indicatorId\":\"" + indicatorId + "\"}",
                tdContext());
        JsonNode root = objectMapper.readTree(json);

        assertThat(root.path("ok").asBoolean()).isTrue();
        assertThat(root.path("data").path("evidence").path("indicatorId").asText())
                .isEqualTo(indicatorId.toString());
        assertThat(root.path("data").path("evidence").path("evidenceId").asText())
                .isEqualTo(evidenceId.toString());
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void executeCheckEvidenceCompleteness_reportsCompleteFlag(boolean complete) throws Exception {
        UUID indicatorId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
        when(checkEvidenceCompletenessUseCase.check(any(), eq(indicatorId)))
                .thenReturn(new CheckEvidenceCompletenessUseCase.CompletenessChecklist(
                        indicatorId,
                        complete,
                        complete,
                        complete,
                        complete,
                        IndicatorState.SUBIDO,
                        complete));

        String json = executor.execute(
                AssistantToolRegistry.CHECK_EVIDENCE_COMPLETENESS_ID,
                "{\"indicatorId\":\"" + indicatorId + "\"}",
                tdContext());
        JsonNode root = objectMapper.readTree(json);

        assertThat(root.path("ok").asBoolean()).isTrue();
        assertThat(root.path("data").path("complete").asBoolean()).isEqualTo(complete);
        assertThat(root.path("data").path("indicatorId").asText()).isEqualTo(indicatorId.toString());
        assertThat(root.path("data").path("currentState").asText()).isEqualTo("SUBIDO");
    }

    private static AssistantAuthContext jdContext() {
        return new AssistantAuthContext(UUID.randomUUID(), "JD", List.of());
    }

    private static AssistantAuthContext tdContext() {
        return new AssistantAuthContext(UUID.randomUUID(), "TD", List.of());
    }

    private static AssistantAuthContext ccContext() {
        UUID programId = UUID.randomUUID();
        return new AssistantAuthContext(UUID.randomUUID(), "CC", List.of(programId));
    }
}
