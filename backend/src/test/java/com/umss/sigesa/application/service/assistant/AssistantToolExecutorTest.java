package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.port.in.ActivateUserUseCase;
import com.umss.sigesa.application.port.in.AddProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.CheckEvidenceCompletenessUseCase;
import com.umss.sigesa.application.port.in.DeactivateUserUseCase;
import com.umss.sigesa.application.port.in.DeleteProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.GetEvidenceDetailUseCase;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.ListPendingEvidencesUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.in.ListProgramsUseCase;
import com.umss.sigesa.application.port.in.ListUsersUseCase;
import com.umss.sigesa.application.port.in.ManageUserProgramAssignmentUseCase;
import com.umss.sigesa.application.port.in.RegisterUserUseCase;
import com.umss.sigesa.application.port.in.ReorderProcessStructureUseCase;
import com.umss.sigesa.application.port.in.SearchNormativeDocumentsUseCase;
import com.umss.sigesa.application.port.in.UpdateProcessPhaseUseCase;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.application.service.assistant.support.AssistantToolExecutorTestFactory;
import com.umss.sigesa.application.service.assistant.support.RecordingAssistantToolAuditPort;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    private AddProcessPhaseUseCase addProcessPhaseUseCase;
    @Mock
    private UpdateProcessPhaseUseCase updateProcessPhaseUseCase;
    @Mock
    private DeleteProcessPhaseUseCase deleteProcessPhaseUseCase;
    @Mock
    private com.umss.sigesa.application.port.in.AddProcessSubphaseUseCase addProcessSubphaseUseCase;
    @Mock
    private com.umss.sigesa.application.port.in.UpdateProcessSubphaseUseCase updateProcessSubphaseUseCase;
    @Mock
    private com.umss.sigesa.application.port.in.DeleteProcessSubphaseUseCase deleteProcessSubphaseUseCase;
    @Mock
    private ReorderProcessStructureUseCase reorderProcessStructureUseCase;
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
                addProcessPhaseUseCase,
                updateProcessPhaseUseCase,
                deleteProcessPhaseUseCase,
                addProcessSubphaseUseCase,
                updateProcessSubphaseUseCase,
                deleteProcessSubphaseUseCase,
                reorderProcessStructureUseCase,
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

    private static AssistantAuthContext jdContext() {
        return new AssistantAuthContext(UUID.randomUUID(), "JD", List.of());
    }

    private static AssistantAuthContext ccContext() {
        return new AssistantAuthContext(UUID.randomUUID(), "CC", List.of(UUID.randomUUID()));
    }
}
