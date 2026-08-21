package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.AssistantAgentProfile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantToolRegistryTest {

    private final AssistantToolRegistry registry = new AssistantToolRegistry();

    @Test
    void toolsForRole_jdIncludesAllTools() {
        var tools = registry.toolsForRole("JD");

        assertThat(tools).extracting(def -> def.id()).containsExactly(
                AssistantToolRegistry.BUSCAR_EVIDENCIAS_ID,
                AssistantToolRegistry.LIST_USERS_ID,
                AssistantToolRegistry.GET_USER_DETAIL_ID,
                AssistantToolRegistry.CREATE_USER_ID,
                AssistantToolRegistry.LIST_PROGRAMS_ID,
                AssistantToolRegistry.LIST_ACTIVE_PROCESSES_ID,
                AssistantToolRegistry.LIST_PROCESS_PHASES_ID,
                AssistantToolRegistry.LIST_PROCESS_STRUCTURE_ID,
                AssistantToolRegistry.SET_USER_STATUS_ID,
                AssistantToolRegistry.MANAGE_USER_STATUS_ID,
                AssistantToolRegistry.MANAGE_USER_ASSIGNMENT_ID,
                AssistantToolRegistry.MANAGE_PROCESS_PHASE_ID,
                AssistantToolRegistry.MANAGE_PROCESS_SUBPHASE_ID,
                AssistantToolRegistry.LIST_PENDING_EVIDENCES_ID,
                AssistantToolRegistry.GET_EVIDENCE_DETAIL_ID,
                AssistantToolRegistry.CHECK_EVIDENCE_COMPLETENESS_ID,
                AssistantToolRegistry.APPROVE_INDICATOR_ID,
                AssistantToolRegistry.REJECT_INDICATOR_ID
        );
    }

    @Test
    void toolsForRole_tdIncludesPhaseAndEvidenceTools() {
        var tools = registry.toolsForRole("TD");

        assertThat(tools).extracting(def -> def.id()).containsExactly(
                AssistantToolRegistry.BUSCAR_EVIDENCIAS_ID,
                AssistantToolRegistry.LIST_PROGRAMS_ID,
                AssistantToolRegistry.LIST_ACTIVE_PROCESSES_ID,
                AssistantToolRegistry.LIST_PROCESS_PHASES_ID,
                AssistantToolRegistry.LIST_PROCESS_STRUCTURE_ID,
                AssistantToolRegistry.MANAGE_PROCESS_PHASE_ID,
                AssistantToolRegistry.MANAGE_PROCESS_SUBPHASE_ID,
                AssistantToolRegistry.LIST_PENDING_EVIDENCES_ID,
                AssistantToolRegistry.GET_EVIDENCE_DETAIL_ID,
                AssistantToolRegistry.CHECK_EVIDENCE_COMPLETENESS_ID,
                AssistantToolRegistry.APPROVE_INDICATOR_ID,
                AssistantToolRegistry.REJECT_INDICATOR_ID
        );
    }

    @Test
    void toolsForRoleAndAgent_phasesProfile_filtersToPhaseTools() {
        var tools = registry.toolsForRoleAndAgent("TD", AssistantAgentProfile.PHASES);

        assertThat(tools).extracting(def -> def.id()).containsExactly(
                AssistantToolRegistry.LIST_PROCESS_PHASES_ID,
                AssistantToolRegistry.LIST_PROCESS_STRUCTURE_ID,
                AssistantToolRegistry.MANAGE_PROCESS_PHASE_ID,
                AssistantToolRegistry.MANAGE_PROCESS_SUBPHASE_ID
        );
    }

    @Test
    void toolsForRoleAndAgent_usersProfile_filtersToUserTools() {
        var tools = registry.toolsForRoleAndAgent("JD", AssistantAgentProfile.USERS);

        assertThat(tools).extracting(def -> def.id()).containsExactly(
                AssistantToolRegistry.LIST_USERS_ID,
                AssistantToolRegistry.GET_USER_DETAIL_ID,
                AssistantToolRegistry.CREATE_USER_ID,
                AssistantToolRegistry.MANAGE_USER_STATUS_ID,
                AssistantToolRegistry.MANAGE_USER_ASSIGNMENT_ID
        );
    }

    @Test
    void toolsForRoleAndAgent_evidenceProfile_filtersToEvidenceTools() {
        var tools = registry.toolsForRoleAndAgent("TD", AssistantAgentProfile.EVIDENCE);

        assertThat(tools).extracting(def -> def.id()).containsExactly(
                AssistantToolRegistry.BUSCAR_EVIDENCIAS_ID,
                AssistantToolRegistry.LIST_PENDING_EVIDENCES_ID,
                AssistantToolRegistry.GET_EVIDENCE_DETAIL_ID,
                AssistantToolRegistry.CHECK_EVIDENCE_COMPLETENESS_ID,
                AssistantToolRegistry.APPROVE_INDICATOR_ID,
                AssistantToolRegistry.REJECT_INDICATOR_ID
        );
    }

    @Test
    void toolsForRole_ccReturnsReadOnlyPhaseAndEvidenceTools() {
        assertThat(registry.toolsForRole("CC")).extracting(def -> def.id()).containsExactly(
                AssistantToolRegistry.BUSCAR_EVIDENCIAS_ID,
                AssistantToolRegistry.LIST_PROCESS_PHASES_ID,
                AssistantToolRegistry.LIST_PROCESS_STRUCTURE_ID,
                AssistantToolRegistry.LIST_PENDING_EVIDENCES_ID,
                AssistantToolRegistry.GET_EVIDENCE_DETAIL_ID,
                AssistantToolRegistry.CHECK_EVIDENCE_COMPLETENESS_ID
        );
    }
}
