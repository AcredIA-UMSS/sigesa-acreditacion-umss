package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.model.assistant.AssistantAgentProfile;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantToolAuditRecord;
import com.umss.sigesa.application.port.in.ListUsersUseCase;
import com.umss.sigesa.application.service.assistant.support.AssistantToolExecutorTestFactory;
import com.umss.sigesa.application.service.assistant.support.RecordingAssistantToolAuditPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistantToolRbacGuardTest {

    private final AssistantToolRegistry registry = new AssistantToolRegistry();
    private final RecordingAssistantToolAuditPort auditPort = new RecordingAssistantToolAuditPort();

    @Mock
    private ListUsersUseCase listUsersUseCase;

    private AssistantToolExecutor executor;

    @BeforeEach
    void setUp() {
        auditPort.clear();
        executor = AssistantToolExecutorTestFactory.createMinimal(registry, auditPort);
    }

    @Test
    void ccCannotExecuteListUsers_evenIfLlmHallucinates() throws Exception {
        String json = executor.execute(
                AssistantToolRegistry.LIST_USERS_ID,
                "{}",
                new AssistantAuthContext(UUID.randomUUID(), "CC", List.of(UUID.randomUUID())),
                AssistantAgentProfile.GENERAL);

        assertThat(json).contains("ACCESS_DENIED");
        assertThat(auditPort.records()).hasSize(1);
        assertThat(auditPort.records().getFirst().success()).isFalse();
        assertThat(auditPort.records().getFirst().outcomeCode()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void jdListUsersDeniedInPhasesAgent_subsetViolation() throws Exception {
        String json = executor.execute(
                AssistantToolRegistry.LIST_USERS_ID,
                "{}",
                new AssistantAuthContext(UUID.randomUUID(), "JD", List.of()),
                AssistantAgentProfile.PHASES);

        assertThat(json).contains("ACCESS_DENIED");
        assertThat(json).contains("agente");
        assertThat(auditPort.records().getFirst().agentId()).isEqualTo("phases");
    }

    @Test
    void ccPhasesAgentCannotExecuteManageProcessPhase() throws Exception {
        String json = executor.execute(
                AssistantToolRegistry.MANAGE_PROCESS_PHASE_ID,
                "{\"action\":\"CREATE\",\"careerQuery\":\"INF-SIS\"}",
                new AssistantAuthContext(UUID.randomUUID(), "CC", List.of(UUID.randomUUID())),
                AssistantAgentProfile.PHASES);

        assertThat(json).contains("ACCESS_DENIED");
    }

    @Test
    void tdUsersAgentSubset_excludesListUsersForTdRole() {
        assertThat(registry.toolsForRoleAndAgent("TD", AssistantAgentProfile.USERS)).isEmpty();
    }

    @Test
    void eeHasNormativeSearchOnly() {
        assertThat(registry.toolsForRole("EE")).extracting(def -> def.id())
                .containsExactly(AssistantToolRegistry.SEARCH_NORMATIVE_DOCS_ID);
    }

    @Test
    void isToolAllowedForAgent_phasesRejectsUserTools() {
        assertThat(registry.isToolAllowedForAgent(AssistantToolRegistry.LIST_USERS_ID, AssistantAgentProfile.PHASES))
                .isFalse();
        assertThat(registry.isToolAllowedForAgent(
                AssistantToolRegistry.LIST_PROCESS_PHASES_ID, AssistantAgentProfile.PHASES))
                .isTrue();
    }

    @Test
    void successfulAuditRecord_logsOkOutcome() throws Exception {
        when(listUsersUseCase.list(isNull(), isNull())).thenReturn(List.of());
        executor = AssistantToolExecutorTestFactory.create(registry, listUsersUseCase, auditPort);

        executor.execute(
                AssistantToolRegistry.LIST_USERS_ID,
                "{}",
                new AssistantAuthContext(UUID.randomUUID(), "JD", List.of()),
                AssistantAgentProfile.USERS);

        AssistantToolAuditRecord record = auditPort.records().getFirst();
        assertThat(record.success()).isTrue();
        assertThat(record.outcomeCode()).isEqualTo("OK");
        assertThat(record.toolId()).isEqualTo(AssistantToolRegistry.LIST_USERS_ID);
        assertThat(record.agentId()).isEqualTo("users");
    }
}
