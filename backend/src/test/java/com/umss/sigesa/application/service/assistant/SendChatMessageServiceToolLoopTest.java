package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantChatContext;
import com.umss.sigesa.application.model.assistant.AssistantChatResult;
import com.umss.sigesa.application.model.assistant.AssistantResolutionPath;
import com.umss.sigesa.application.model.assistant.ChatCompletionRequest;
import com.umss.sigesa.application.model.assistant.ChatCompletionResult;
import com.umss.sigesa.application.model.assistant.ToolCall;
import com.umss.sigesa.application.port.out.ChatCompletionPort;
import com.umss.sigesa.domain.model.ChatMessage;
import com.umss.sigesa.domain.model.ChatRole;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendChatMessageServiceToolLoopTest {

    private static final String PHASES_TOOL_JSON = """
            {"ok":true,"data":{"careerName":"Ingeniería de Sistemas","careerCode":"INF-SIS",\
            "templateType":"CEUB","phases":[{"order":1,"name":"Fase 1","phaseId":"p1","subphaseCount":2}]},"error":null}
            """;

    private static final String STRUCTURE_TOOL_JSON = """
            {"ok":true,"data":{"careerName":"Ingeniería de Sistemas","templateType":"CEUB",\
            "phases":[{"order":1,"name":"Fase 1","subphases":[{"name":"Matriz de evidencias"}]}]},"error":null}
            """;

    private static final String NORMATIVE_TOOL_JSON = """
            {"ok":true,"data":{"documents":[{"title":"Matriz CEUB","snippet":"Requisitos matriz",\
            "templateType":"CEUB"}]},"error":null}
            """;

    @Mock
    private ChatCompletionPort chatCompletionPort;

    @Mock
    private AssistantToolExecutor toolExecutor;

    private AssistantToolRegistry toolRegistry;
    private AssistantKeywordRouter keywordRouter;
    private SendChatMessageService serviceWithLlm;
    private SendChatMessageService serviceWithoutLlm;

    private AssistantNormativeRagService disabledRagService() {
        return new AssistantNormativeRagService((query, templateType, limit) -> List.of(), false, 3);
    }

    @BeforeEach
    void setUp() {
        toolRegistry = new AssistantToolRegistry();
        keywordRouter = new AssistantKeywordRouter();
        AssistantNormativeRagService ragService = disabledRagService();
        serviceWithLlm = new SendChatMessageService(
                chatCompletionPort,
                toolRegistry,
                toolExecutor,
                keywordRouter,
                new ObjectMapper(),
                "system prompt",
                true,
                3,
                ragService
        );
        serviceWithoutLlm = new SendChatMessageService(
                chatCompletionPort,
                toolRegistry,
                toolExecutor,
                keywordRouter,
                new ObjectMapper(),
                "system prompt",
                false,
                3,
                ragService
        );
    }

    @Test
    void scenario1_controlledKeyword_ccRole_doesNotCallLlm() {
        AssistantAuthContext auth = ccContext();
        when(toolExecutor.execute(
                eq(AssistantToolRegistry.LIST_PROCESS_PHASES_ID),
                any(),
                eq(auth),
                any())).thenReturn(PHASES_TOOL_JSON);

        AssistantChatResult result = serviceWithLlm.send(
                "Lista las fases de Ingeniería de Sistemas CEUB",
                List.of(),
                auth,
                AssistantChatContext.general());

        assertThat(result.path()).isEqualTo(AssistantResolutionPath.KEYWORD);
        assertThat(result.llmInvoked()).isFalse();
        verify(chatCompletionPort, never()).complete(any());
    }

    @Test
    void scenario1_controlledKeyword_doesNotCallLlm() {
        AssistantAuthContext auth = tdContext();
        when(toolExecutor.execute(
                eq(AssistantToolRegistry.LIST_PROCESS_PHASES_ID),
                any(),
                eq(auth),
                any())).thenReturn(PHASES_TOOL_JSON);

        AssistantChatResult result = serviceWithLlm.send(
                "Lista las fases de Ingeniería de Sistemas CEUB",
                List.of(),
                auth,
                AssistantChatContext.general());

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
                )))
                .thenReturn(new ChatCompletionResult("", List.of()));
        when(toolExecutor.execute(
                eq(AssistantToolRegistry.LIST_PROCESS_PHASES_ID),
                eq("{\"careerQuery\":\"Ingeniería de Sistemas\",\"templateType\":\"CEUB\"}"),
                eq(auth),
                any())).thenReturn(PHASES_TOOL_JSON);

        AssistantChatResult result = serviceWithLlm.send(
                "¿Qué etapas tiene el proceso activo de Ingeniería de Sistemas CEUB?",
                List.of(),
                auth,
                AssistantChatContext.general());

        assertThat(result.path()).isEqualTo(AssistantResolutionPath.LLM);
        assertThat(result.llmInvoked()).isTrue();
        assertThat(result.toolId()).isEqualTo(AssistantToolRegistry.LIST_PROCESS_PHASES_ID);
        assertThat(result.reply()).contains("Fase 1");
        verify(chatCompletionPort, times(2)).complete(any());
    }

    @Test
    void scenario3_outOfScope_noToolNoInventedData() {
        AssistantChatResult result = serviceWithLlm.send(
                "¿Cuál es el presupuesto de la universidad para 2027?",
                List.of(),
                tdContext(),
                AssistantChatContext.general());

        assertThat(result.path()).isEqualTo(AssistantResolutionPath.OUT_OF_SCOPE);
        assertThat(result.toolId()).isNull();
        assertThat(result.sourceTables()).isEmpty();
        assertThat(result.reply()).contains("No puedo responder eso");
        assertThat(result.reply()).contains("Puedo ayudarte con:");
        verify(toolExecutor, never()).execute(any(), any(), any(), any());
        verify(chatCompletionPort, never()).complete(any());
    }

    @Test
    void scenario4_llmDisabled_sameKeywordQuestionStillWorks() {
        AssistantAuthContext auth = tdContext();
        when(toolExecutor.execute(
                eq(AssistantToolRegistry.LIST_PROCESS_PHASES_ID),
                any(),
                eq(auth),
                any())).thenReturn(PHASES_TOOL_JSON);

        AssistantChatResult result = serviceWithoutLlm.send(
                "Lista las fases de Ingeniería de Sistemas CEUB",
                List.of(),
                auth,
                AssistantChatContext.general());

        assertThat(result.path()).isEqualTo(AssistantResolutionPath.KEYWORD);
        assertThat(result.reply()).contains("Fase 1");
        verify(chatCompletionPort, never()).complete(any());
    }

    @Test
    void phasesAgent_contextualListPhases_doesNotCallLlm() {
        AssistantAuthContext auth = tdContext();
        UUID processId = UUID.fromString("950e8400-e29b-41d4-a716-446655440020");
        AssistantChatContext context = AssistantChatContext.phases(
                processId, "Ingeniería de Sistemas", "INF-SIS", "CEUB");
        when(toolExecutor.execute(
                eq(AssistantToolRegistry.LIST_PROCESS_PHASES_ID),
                any(),
                eq(auth),
                any())).thenReturn(PHASES_TOOL_JSON);

        AssistantChatResult result = serviceWithLlm.send(
                "Lista las fases de este proceso",
                List.of(),
                auth,
                context);

        assertThat(result.path()).isEqualTo(AssistantResolutionPath.KEYWORD);
        assertThat(result.toolId()).isEqualTo(AssistantToolRegistry.LIST_PROCESS_PHASES_ID);
        verify(chatCompletionPort, never()).complete(any());
    }

    @Test
    void phasesAgent_llmSelection_onlyPhaseTools() {
        when(chatCompletionPort.complete(any())).thenReturn(new ChatCompletionResult("", List.of()));

        serviceWithLlm.send(
                "Renombra la Fase 2",
                List.of(),
                tdContext(),
                AssistantChatContext.phases(
                        UUID.randomUUID(), "Ingeniería de Sistemas", "INF-SIS", "CEUB"));

        ArgumentCaptor<ChatCompletionRequest> captor = ArgumentCaptor.forClass(ChatCompletionRequest.class);
        verify(chatCompletionPort).complete(captor.capture());
        assertThat(captor.getValue().tools()).extracting(def -> def.id()).containsExactly(
                AssistantToolRegistry.LIST_PROCESS_PHASES_ID,
                AssistantToolRegistry.LIST_PROCESS_STRUCTURE_ID,
                AssistantToolRegistry.MANAGE_PROCESS_PHASE_ID,
                AssistantToolRegistry.MANAGE_PROCESS_SUBPHASE_ID,
                AssistantToolRegistry.SEARCH_NORMATIVE_DOCS_ID);
    }

    @Test
    void scenario4_llmDisabled_synonymFallsToOutOfScope() {
        AssistantChatResult result = serviceWithoutLlm.send(
                "¿Qué etapas tiene el proceso activo de Ingeniería de Sistemas CEUB?",
                List.of(),
                tdContext(),
                AssistantChatContext.general());

        assertThat(result.path()).isEqualTo(AssistantResolutionPath.OUT_OF_SCOPE);
        assertThat(result.reply()).contains("SIGESA_ASSISTANT_LLM_ENABLED=false");
        verify(chatCompletionPort, never()).complete(any());
    }

    @Test
    void send_jdRequestIncludesToolsInLlmSelection() {
        when(chatCompletionPort.complete(any())).thenReturn(new ChatCompletionResult("", List.of()));

        serviceWithLlm.send(
                "¿Qué etapas tiene Ingeniería de Sistemas?",
                List.of(),
                jdContext(),
                AssistantChatContext.general());

        ArgumentCaptor<ChatCompletionRequest> captor = ArgumentCaptor.forClass(ChatCompletionRequest.class);
        verify(chatCompletionPort).complete(captor.capture());
        assertThat(captor.getValue().tools()).hasSize(16);
    }

    @Test
    void multiToolLoop_chainsTwoToolsAndReturnsTrace() {
        AssistantAuthContext auth = tdContext();
        when(chatCompletionPort.complete(any()))
                .thenReturn(new ChatCompletionResult(null, List.of(
                        new ToolCall("call_1", AssistantToolRegistry.LIST_PROCESS_STRUCTURE_ID,
                                "{\"careerQuery\":\"Ingeniería de Sistemas\",\"templateType\":\"CEUB\"}")
                )))
                .thenReturn(new ChatCompletionResult(null, List.of(
                        new ToolCall("call_2", AssistantToolRegistry.SEARCH_NORMATIVE_DOCS_ID,
                                "{\"query\":\"matriz evidencias CEUB\",\"templateType\":\"CEUB\"}")
                )))
                .thenReturn(new ChatCompletionResult("", List.of()));
        when(toolExecutor.execute(
                eq(AssistantToolRegistry.LIST_PROCESS_STRUCTURE_ID),
                any(),
                eq(auth),
                any())).thenReturn(STRUCTURE_TOOL_JSON);
        when(toolExecutor.execute(
                eq(AssistantToolRegistry.SEARCH_NORMATIVE_DOCS_ID),
                any(),
                eq(auth),
                any())).thenReturn(NORMATIVE_TOOL_JSON);

        AssistantChatResult result = serviceWithLlm.send(
                "Muestra la estructura de Ingeniería de Sistemas CEUB y busca normativa de Matriz de evidencias",
                List.of(),
                auth,
                AssistantChatContext.general());

        assertThat(result.path()).isEqualTo(AssistantResolutionPath.LLM);
        assertThat(result.llmInvoked()).isTrue();
        assertThat(result.steps()).hasSize(2);
        assertThat(result.steps().get(0).toolId()).isEqualTo(AssistantToolRegistry.LIST_PROCESS_STRUCTURE_ID);
        assertThat(result.steps().get(1).toolId()).isEqualTo(AssistantToolRegistry.SEARCH_NORMATIVE_DOCS_ID);
        assertThat(result.reply()).contains("Paso 1");
        assertThat(result.reply()).contains("Paso 2");
        verify(chatCompletionPort, times(3)).complete(any());
    }

    @Test
    void multiToolLoop_respectsMaxIterations() {
        AssistantAuthContext auth = tdContext();
        SendChatMessageService limitedService = new SendChatMessageService(
                chatCompletionPort,
                toolRegistry,
                toolExecutor,
                keywordRouter,
                new ObjectMapper(),
                "system prompt",
                true,
                1,
                disabledRagService());
        when(chatCompletionPort.complete(any()))
                .thenReturn(new ChatCompletionResult(null, List.of(
                        new ToolCall("call_1", AssistantToolRegistry.LIST_PROCESS_PHASES_ID,
                                "{\"careerQuery\":\"Ingeniería de Sistemas\",\"templateType\":\"CEUB\"}")
                )));
        when(toolExecutor.execute(
                eq(AssistantToolRegistry.LIST_PROCESS_PHASES_ID),
                any(),
                eq(auth),
                any())).thenReturn(PHASES_TOOL_JSON);

        AssistantChatResult result = limitedService.send(
                "Consulta usuarios CC y además lista programas académicos",
                List.of(),
                auth,
                AssistantChatContext.general());

        assertThat(result.steps()).hasSize(1);
        assertThat(result.reply()).contains("Límite de 1 pasos");
        verify(chatCompletionPort, times(1)).complete(any());
    }

    private static final String SUBPHASE_PREVIEW_JSON = """
            {"ok":true,"data":{"confirmationRequired":true,"action":"CREATE_SUBPHASE",\
            "message":"Vista previa de subfase.","preview":{"requestedAction":"CREATE_SUBPHASE",\
            "name":"Evidencia docente","assignedOrder":2,"phaseName":"Fase 2"}},\
            "error":null}
            """;

    @Test
    void writeToolPreview_stopsLoop_waitsForUserConfirmation() {
        AssistantAuthContext auth = tdContext();
        UUID processId = UUID.fromString("950e8400-e29b-41d4-a716-446655440020");
        AssistantChatContext context = AssistantChatContext.phases(
                processId, "Ingeniería de Sistemas", "INF-SIS", "CEUB");

        when(chatCompletionPort.complete(any())).thenReturn(new ChatCompletionResult(null, List.of(
                new ToolCall(
                        "call_1",
                        AssistantToolRegistry.MANAGE_PROCESS_SUBPHASE_ID,
                        "{\"action\":\"CREATE\",\"phaseOrder\":2,\"name\":\"Evidencia docente\","
                                + "\"referenceUrl\":\"https://example.com/evidencia_docente\",\"confirmed\":false}")
        )));
        when(toolExecutor.execute(
                eq(AssistantToolRegistry.MANAGE_PROCESS_SUBPHASE_ID),
                any(),
                eq(auth),
                any())).thenReturn(SUBPHASE_PREVIEW_JSON);

        AssistantChatResult result = serviceWithLlm.send(
                "Agrega una subfase «Evidencia docente» con enlace HTTPS en la Fase 2",
                List.of(),
                auth,
                context);

        assertThat(result.steps()).hasSize(1);
        assertThat(result.reply()).contains("confirmo");
        assertThat(result.reply()).doesNotContain("Paso 2");
        assertThat(result.reply()).doesNotContain("creada");
        verify(chatCompletionPort, times(1)).complete(any());
        verify(toolExecutor, times(1)).execute(
                eq(AssistantToolRegistry.MANAGE_PROCESS_SUBPHASE_ID),
                any(),
                eq(auth),
                any());
    }

    private static final String SUBPHASE_EXECUTED_JSON = """
            {"ok":true,"data":{"confirmationRequired":false,"executed":true,\
            "message":"Subfase «Evidencia docente» creada en «Fase 2» con orden 2."},\
            "error":null}
            """;

    @Test
    void phasesAgent_confirmo_afterPreview_executesViaKeywordWithoutLlm() {
        AssistantAuthContext auth = tdContext();
        UUID processId = UUID.fromString("950e8400-e29b-41d4-a716-446655440020");
        AssistantChatContext context = AssistantChatContext.phases(
                processId, "Ingeniería de Sistemas", "INF-SIS", "CEUB");

        String previewReply = """
                La fase «Fase 2.: verificacion de evidencias actualizada» tiene **1** subfase(s).
                Enlace: https://example.com/evidencia_docente

                Resumen: «Evidencia docente» → orden 2 en «Fase 2.: verificacion de evidencias actualizada».

                Responda **confirmo** para ejecutar la acción.""";

        List<ChatMessage> history = List.of(
                new ChatMessage(ChatRole.USER, "Agrega una subfase «Evidencia docente» en Fase 2"),
                new ChatMessage(ChatRole.ASSISTANT, previewReply));

        when(toolExecutor.execute(
                eq(AssistantToolRegistry.MANAGE_PROCESS_SUBPHASE_ID),
                org.mockito.ArgumentMatchers.argThat(json -> json != null && json.contains("\"confirmed\":true")),
                eq(auth),
                any())).thenReturn(SUBPHASE_EXECUTED_JSON);

        AssistantChatResult result = serviceWithLlm.send("confirmo", history, auth, context);

        assertThat(result.path()).isEqualTo(AssistantResolutionPath.KEYWORD);
        assertThat(result.llmInvoked()).isFalse();
        assertThat(result.reply()).contains("creada");
        verify(chatCompletionPort, never()).complete(any());
    }

    private static AssistantAuthContext jdContext() {
        return new AssistantAuthContext(UUID.randomUUID(), "JD", List.of());
    }

    private static AssistantAuthContext tdContext() {
        return new AssistantAuthContext(UUID.randomUUID(), "TD", List.of());
    }

    private static AssistantAuthContext ccContext() {
        return new AssistantAuthContext(UUID.randomUUID(), "CC", List.of(UUID.randomUUID()));
    }
}
