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
                AssistantToolRegistry.LIST_USERS_ID,
                AssistantToolRegistry.LIST_PROGRAMS_ID,
                AssistantToolRegistry.LIST_ACTIVE_PROCESSES_ID,
                AssistantToolRegistry.LIST_PROCESS_PHASES_ID,
                AssistantToolRegistry.LIST_PROCESS_STRUCTURE_ID,
                AssistantToolRegistry.SET_USER_STATUS_ID,
                AssistantToolRegistry.MANAGE_PROCESS_PHASE_ID,
                AssistantToolRegistry.MANAGE_PROCESS_SUBPHASE_ID
        );
    }

    @Test
    void toolsForRole_tdIncludesPhaseToolsOnly() {
        var tools = registry.toolsForRole("TD");

        assertThat(tools).extracting(def -> def.id()).containsExactly(
                AssistantToolRegistry.LIST_PROGRAMS_ID,
                AssistantToolRegistry.LIST_ACTIVE_PROCESSES_ID,
                AssistantToolRegistry.LIST_PROCESS_PHASES_ID,
                AssistantToolRegistry.LIST_PROCESS_STRUCTURE_ID,
                AssistantToolRegistry.MANAGE_PROCESS_PHASE_ID,
                AssistantToolRegistry.MANAGE_PROCESS_SUBPHASE_ID
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
    void toolsForRole_ccReturnsReadOnlyPhaseTools() {
        assertThat(registry.toolsForRole("CC")).extracting(def -> def.id()).containsExactly(
                AssistantToolRegistry.LIST_PROCESS_PHASES_ID,
                AssistantToolRegistry.LIST_PROCESS_STRUCTURE_ID
        );
    }
}
