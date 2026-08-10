package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SendChatMessageServiceToolLoopTest {

    @Mock
    private ChatCompletionPort chatCompletionPort;

    @Mock
    private AssistantToolExecutor toolExecutor;

    @Mock
    private AssistantDirectQueryService directQueryService;

    private AssistantToolRegistry toolRegistry;
    private SendChatMessageService service;

    @BeforeEach
    void setUp() {
        toolRegistry = new AssistantToolRegistry();
        when(directQueryService.tryHandle(any(), any(), any())).thenReturn(java.util.Optional.empty());
        service = new SendChatMessageService(
                chatCompletionPort,
                toolRegistry,
                toolExecutor,
                directQueryService,
                "system prompt",
                3
        );
    }

    @Test
    void send_withoutToolCallsReturnsDirectReply() {
        when(chatCompletionPort.complete(any())).thenReturn(new ChatCompletionResult("Hola, ¿en qué puedo ayudar?", List.of()));

        String reply = service.send("Hola", List.of(), jdContext());

        assertThat(reply).isEqualTo("Hola, ¿en qué puedo ayudar?");
    }

    @Test
    void send_withSingleToolCallExecutesToolAndReturnsFinalReply() {
        AssistantAuthContext auth = jdContext();
        when(chatCompletionPort.complete(any()))
                .thenReturn(new ChatCompletionResult(null, List.of(
                        new ToolCall("call_1", "list_users", "{\"role\":\"CC\",\"status\":\"ACTIVE\"}")
                )))
                .thenReturn(new ChatCompletionResult("Hay 2 coordinadores activos.", List.of()));

        when(toolExecutor.execute("list_users", "{\"role\":\"CC\",\"status\":\"ACTIVE\"}", auth))
                .thenReturn("{\"ok\":true,\"data\":{\"users\":[],\"total\":0},\"error\":null}");

        String reply = service.send("¿Qué CC activos hay?", List.of(), auth);

        assertThat(reply).isEqualTo("Hay 2 coordinadores activos.");
        verify(toolExecutor).execute("list_users", "{\"role\":\"CC\",\"status\":\"ACTIVE\"}", auth);
    }

    @Test
    void send_jdRequestIncludesToolsInCompletionRequest() {
        when(chatCompletionPort.complete(any())).thenReturn(new ChatCompletionResult("Respuesta directa.", List.of()));

        service.send("Lista usuarios", List.of(), jdContext());

        ArgumentCaptor<ChatCompletionRequest> captor = ArgumentCaptor.forClass(ChatCompletionRequest.class);
        verify(chatCompletionPort).complete(captor.capture());
        assertThat(captor.getValue().tools()).hasSize(6);
        assertThat(captor.getValue().tools().getFirst().id()).isEqualTo("list_users");
    }

    @Test
    void send_tdRequestIncludesPhaseToolsOnly() {
        when(chatCompletionPort.complete(any())).thenReturn(new ChatCompletionResult("Respuesta directa.", List.of()));

        service.send("Lista fases de Sistemas", List.of(), tdContext());

        ArgumentCaptor<ChatCompletionRequest> captor = ArgumentCaptor.forClass(ChatCompletionRequest.class);
        verify(chatCompletionPort).complete(captor.capture());
        assertThat(captor.getValue().tools()).hasSize(4);
        assertThat(captor.getValue().tools()).extracting(tool -> tool.id()).containsExactly(
                AssistantToolRegistry.LIST_PROGRAMS_ID,
                AssistantToolRegistry.LIST_ACTIVE_PROCESSES_ID,
                AssistantToolRegistry.LIST_PROCESS_PHASES_ID,
                AssistantToolRegistry.MANAGE_PROCESS_PHASE_ID
        );
    }

    @Test
    void send_usesDirectQueryWhenAvailable() {
        when(directQueryService.tryHandle(any(), any(), any()))
                .thenReturn(java.util.Optional.of("Respuesta directa del sistema."));

        String reply = service.send("Lista procesos activos", List.of(), jdContext());

        assertThat(reply).isEqualTo("Respuesta directa del sistema.");
        verify(chatCompletionPort, never()).complete(any());
    }

    @Test
    void send_ccRequestDoesNotIncludeTools() {
        when(chatCompletionPort.complete(any())).thenReturn(new ChatCompletionResult("No tengo acceso a esa información.", List.of()));

        service.send("Lista usuarios", List.of(), ccContext());

        ArgumentCaptor<ChatCompletionRequest> captor = ArgumentCaptor.forClass(ChatCompletionRequest.class);
        verify(chatCompletionPort).complete(captor.capture());
        assertThat(captor.getValue().tools()).isEmpty();
    }

    @Test
    void send_exceedingMaxIterationsReturnsFallback() {
        ToolCall toolCall = new ToolCall("call_1", "list_users", "{}");
        when(chatCompletionPort.complete(any()))
                .thenReturn(new ChatCompletionResult(null, List.of(toolCall)));
        when(toolExecutor.execute(any(), any(), any()))
                .thenReturn("{\"ok\":true,\"data\":{\"users\":[],\"total\":0},\"error\":null}");

        SendChatMessageService limitedService = new SendChatMessageService(
                chatCompletionPort,
                toolRegistry,
                toolExecutor,
                directQueryService,
                "system prompt",
                3
        );

        String reply = limitedService.send("Loop infinito", List.of(), jdContext());

        assertThat(reply).contains("número máximo de pasos");
    }

    private static AssistantAuthContext jdContext() {
        return new AssistantAuthContext(UUID.randomUUID(), "JD", List.of());
    }

    private static AssistantAuthContext ccContext() {
        return new AssistantAuthContext(UUID.randomUUID(), "CC", List.of());
    }

    private static AssistantAuthContext tdContext() {
        return new AssistantAuthContext(UUID.randomUUID(), "TD", List.of());
    }
}
