package com.sigesa.app.contracts.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.sigesa.app.contracts.JsonContracts;
import com.umss.sigesa.adapter.in.web.dto.AssistantChatContextDto;
import com.umss.sigesa.adapter.in.web.dto.AssistantToolStepResponse;
import com.umss.sigesa.adapter.in.web.dto.ChatMessageDto;
import com.umss.sigesa.adapter.in.web.dto.SendChatMessageRequest;
import com.umss.sigesa.adapter.in.web.dto.SendChatMessageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Contrato JSON POST /api/v1/assistant/chat")
class SendChatMessageJsonContractTest {

    @Test
    void chatRequest_requiereMessageYAceptaHistoryYContext() {
        UUID processId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        JsonNode json = JsonContracts.tree(new SendChatMessageRequest(
                "evidencias pendientes",
                List.of(new ChatMessageDto("user", "hola")),
                new AssistantChatContextDto("evidence", processId, null, null, null, null, null)));
        JsonContracts.assertObjectFields(json, "message", "history", "context");
        assertThat(json.get("message").asText()).isEqualTo("evidencias pendientes");
        JsonContracts.assertObjectFields(json.get("history").get(0), "role", "content");
        JsonNode context = json.get("context");
        assertThat(context.get("agent").asText()).isEqualTo("evidence");
        assertThat(context.get("processId").asText()).isEqualTo(processId.toString());
        assertThat(context.get("agent").asText()).isIn("general", "phases", "users", "evidence");
    }

    @Test
    void chatResponse_requiereReplyPathLlmYSteps() {
        JsonNode json = JsonContracts.tree(new SendChatMessageResponse(
                "Hay 2 evidencias pendientes.",
                "list_pending_evidences",
                List.of("evidence"),
                "KEYWORD",
                false,
                List.of(new AssistantToolStepResponse(1, "list_pending_evidences", List.of("evidence"), true))));
        JsonContracts.assertObjectFields(json, "reply", "toolId", "sourceTables", "path", "llmInvoked", "steps");
        assertThat(json.get("reply").isTextual()).isTrue();
        assertThat(json.get("path").asText()).isIn("KEYWORD", "LLM", "RAG", "OUT_OF_SCOPE");
        assertThat(json.get("llmInvoked").isBoolean()).isTrue();
        assertThat(json.get("steps").isArray()).isTrue();
        JsonContracts.assertObjectFields(json.get("steps").get(0), "step", "toolId", "sourceTables", "success");
        assertThat(json.get("steps").get(0).get("success").asBoolean()).isTrue();
    }
}
