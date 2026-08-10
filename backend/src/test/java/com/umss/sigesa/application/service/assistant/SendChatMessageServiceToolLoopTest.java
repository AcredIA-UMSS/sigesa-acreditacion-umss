package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantChatResult;
import com.umss.sigesa.application.model.assistant.AssistantResolutionPath;
import com.umss.sigesa.application.model.assistant.ChatCompletionRequest;
import com.umss.sigesa.application.model.assistant.ChatCompletionResult;
import com.umss.sigesa.application.model.assistant.ToolCall;
import com.umss.sigesa.application.port.out.ChatCompletionPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendChatMessageServiceToolLoopTest {

    private static final String PHASES_TOOL_JSON = """
            {"ok":true,"data":{"careerName":"Ingeniería de Sistemas","careerCode":"INF-SIS",\
            "templateType":"CEUB","phases":[{"order":1,"name":"Fase 1","phaseId":"p1","subphaseCount":2}]},"error":null}
            """;

    @Mock
    private ChatCompletionPort chatCompletionPort;

    @Mock
    private AssistantToolExecutor toolExecutor;

    private AssistantToolRegistry toolRegistry;
    private AssistantKeywordRouter keywordRouter;
    private SendChatMessageService serviceWithLlm;
    private SendChatMessageService serviceWithoutLlm;

    @BeforeEach
    void setUp() {
        toolRegistry = new AssistantToolRegistry();
        keywordRouter = new AssistantKeywordRouter();
        serviceWithLlm = new SendChatMessageService(
                chatCompletionPort,
                toolRegistry,
                toolExecutor,
                keywordRouter,
                new ObjectMapper(),
                "system prompt",
                true
        );
        serviceWithoutLlm = new SendChatMessageService(
                chatCompletionPort,
                toolRegistry,
                toolExecutor,
                keywordRouter,
                new ObjectMapper(),
                "system prompt",
                false
        );
    }

    @Test
    void scenario1_controlledKeyword_doesNotCallLlm() {
        AssistantAuthContext auth = tdContext();
        when(toolExecutor.execute(
                AssistantToolRegistry.LIST_PROCESS_PHASES_ID,
                any(),
                auth)).thenReturn(PHASES_TOOL_JSON);

        AssistantChatResult result = serviceWithLlm.send(
                "Lista las fases de Ingeniería de Sistemas CEUB",
                List.of(),
                auth);

        assertThat(result.path()).isEqualTo(AssistantResolutionPath.KEYWORD);
        assertThat(result.llmInvoked()).isFalse();
        assertThat(result.toolId()).isEqualTo(AssistantToolRegistry.LIST_PROCESS_PHASES_ID);
        assertThat(result.sourceTables()).contains("phases");
        assertThat(result.reply()).contains("Fase 1");
        verify(chatCompletionPort, never()).complete(any());
    }

    @Test
    void scenario2_synonym_usesLlmToPickTool_codeFormatsAnswer() {
        AssistantAuthContext auth = tdContext();
        when(chatCompletionPort.complete(any()))
                .thenReturn(new ChatCompletionResult(null, List.of(
                        new ToolCall("call_1", AssistantToolRegistry.LIST_PROCESS_PHASES_ID,
                                "{\"careerQuery\":\"Ingeniería de Sistemas\",\"templateType\":\"CEUB\"}")
                )));
        when(toolExecutor.execute(
                AssistantToolRegistry.LIST_PROCESS_PHASES_ID,
                "{\"careerQuery\":\"Ingeniería de Sistemas\",\"templateType\":\"CEUB\"}",
                auth)).thenReturn(PHASES_TOOL_JSON);

        AssistantChatResult result = serviceWithLlm.send(
                "¿Qué etapas tiene el proceso activo de Ingeniería de Sistemas CEUB?",
                List.of(),
                auth);

        assertThat(result.path()).isEqualTo(AssistantResolutionPath.LLM);
        assertThat(result.llmInvoked()).isTrue();
        assertThat(result.toolId()).isEqualTo(AssistantToolRegistry.LIST_PROCESS_PHASES_ID);
        assertThat(result.reply()).contains("Fase 1");
        verify(chatCompletionPort).complete(any());
    }

    @Test
    void scenario3_outOfScope_noToolNoInventedData() {
        AssistantChatResult result = serviceWithLlm.send(
                "¿Cuál es el presupuesto de la universidad para 2027?",
                List.of(),
                tdContext());

        assertThat(result.path()).isEqualTo(AssistantResolutionPath.OUT_OF_SCOPE);
        assertThat(result.toolId()).isNull();
        assertThat(result.sourceTables()).isEmpty();
        assertThat(result.reply()).contains("No puedo responder eso");
        assertThat(result.reply()).contains("Puedo ayudarte con:");
        verify(toolExecutor, never()).execute(any(), any(), any());
        verify(chatCompletionPort, never()).complete(any());
    }

    @Test
    void scenario4_llmDisabled_sameKeywordQuestionStillWorks() {
        AssistantAuthContext auth = tdContext();
        when(toolExecutor.execute(
                AssistantToolRegistry.LIST_PROCESS_PHASES_ID,
                any(),
                auth)).thenReturn(PHASES_TOOL_JSON);

        AssistantChatResult result = serviceWithoutLlm.send(
                "Lista las fases de Ingeniería de Sistemas CEUB",
                List.of(),
                auth);

        assertThat(result.path()).isEqualTo(AssistantResolutionPath.KEYWORD);
        assertThat(result.reply()).contains("Fase 1");
        verify(chatCompletionPort, never()).complete(any());
    }

    @Test
    void scenario4_llmDisabled_synonymFallsToOutOfScope() {
        AssistantChatResult result = serviceWithoutLlm.send(
                "¿Qué etapas tiene el proceso activo de Ingeniería de Sistemas CEUB?",
                List.of(),
                tdContext());

        assertThat(result.path()).isEqualTo(AssistantResolutionPath.OUT_OF_SCOPE);
        assertThat(result.reply()).contains("SIGESA_ASSISTANT_LLM_ENABLED=false");
        verify(chatCompletionPort, never()).complete(any());
    }

    @Test
    void send_jdRequestIncludesToolsInLlmSelection() {
        when(chatCompletionPort.complete(any())).thenReturn(new ChatCompletionResult("", List.of()));

        serviceWithLlm.send("¿Qué etapas tiene Ingeniería de Sistemas?", List.of(), jdContext());

        ArgumentCaptor<ChatCompletionRequest> captor = ArgumentCaptor.forClass(ChatCompletionRequest.class);
        verify(chatCompletionPort).complete(captor.capture());
        assertThat(captor.getValue().tools()).hasSize(6);
    }

    private static AssistantAuthContext jdContext() {
        return new AssistantAuthContext(UUID.randomUUID(), "JD", List.of());
    }

    private static AssistantAuthContext tdContext() {
        return new AssistantAuthContext(UUID.randomUUID(), "TD", List.of());
    }
}
