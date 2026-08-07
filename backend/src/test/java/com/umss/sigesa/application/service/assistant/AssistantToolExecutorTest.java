package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.port.in.ListUsersUseCase;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistantToolExecutorTest {

    @Mock
    private ListUsersUseCase listUsersUseCase;

    private AssistantToolExecutor executor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        executor = new AssistantToolExecutor(new AssistantToolRegistry(), listUsersUseCase, objectMapper);
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

        String json = executor.execute("list_users", "{}", jdContext());
        JsonNode root = objectMapper.readTree(json);

        assertThat(root.path("ok").asBoolean()).isTrue();
        assertThat(root.path("data").path("total").asInt()).isEqualTo(1);
        assertThat(root.path("data").path("users").get(0).path("email").asText())
                .isEqualTo("cc@umss.edu.bo");
    }

    @Test
    void executeListUsers_withCcReturnsAccessDenied() throws Exception {
        String json = executor.execute("list_users", "{}", ccContext());
        JsonNode root = objectMapper.readTree(json);

        assertThat(root.path("ok").asBoolean()).isFalse();
        assertThat(root.path("error").path("code").asText()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void executeListUsers_withInvalidRoleReturnsError() throws Exception {
        when(listUsersUseCase.list(any(), any())).thenThrow(new InvalidRoleException("ADMIN"));

        String json = executor.execute("list_users", "{\"role\":\"ADMIN\"}", jdContext());
        JsonNode root = objectMapper.readTree(json);

        assertThat(root.path("ok").asBoolean()).isFalse();
        assertThat(root.path("error").path("code").asText()).isEqualTo("INVALID_ROLE");
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
