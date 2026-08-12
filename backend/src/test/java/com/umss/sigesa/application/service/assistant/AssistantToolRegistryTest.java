package com.umss.sigesa.application.service.assistant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantToolRegistryTest {

    private final AssistantToolRegistry registry = new AssistantToolRegistry();

    @Test
    void toolsForRole_jdIncludesListUsers() {
        var tools = registry.toolsForRole("JD");

        assertThat(tools).hasSize(1);
        assertThat(tools.getFirst().id()).isEqualTo("list_users");
        assertThat(tools.getFirst().allowedRoles()).containsExactly("JD");
    }

    @Test
    void toolsForRole_ccReturnsEmpty() {
        assertThat(registry.toolsForRole("CC")).isEmpty();
    }

    @Test
    void toolsForRole_tdReturnsEmpty() {
        assertThat(registry.toolsForRole("TD")).isEmpty();
    }

    @Test
    void findById_returnsListUsersDefinition() {
        assertThat(registry.findById("list_users"))
                .isPresent()
                .get()
                .extracting(def -> def.id())
                .isEqualTo("list_users");
    }
}
