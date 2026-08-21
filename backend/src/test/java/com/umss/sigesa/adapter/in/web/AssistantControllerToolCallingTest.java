package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.AssistantChatContextDto;
import com.umss.sigesa.adapter.in.web.dto.SendChatMessageRequest;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantChatResult;
import com.umss.sigesa.application.model.assistant.AssistantResolutionPath;
import com.umss.sigesa.application.port.in.SendChatMessageUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.assistant.AssistantChatContextFactory;
import com.umss.sigesa.application.service.assistant.AssistantChatInputValidator;
import com.umss.sigesa.domain.exception.AssistantInvalidInputException;
import com.umss.sigesa.config.AssistantProperties;
import com.umss.sigesa.domain.exception.AssistantAgentAccessDeniedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistantControllerToolCallingTest {

    @Mock
    private SendChatMessageUseCase sendChatMessageUseCase;

    @Mock
    private AssistantProperties assistantProperties;

    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    @Mock
    private AssistantChatContextFactory chatContextFactory;

    private AssistantChatInputValidator chatInputValidator;

    private AssistantController controller;

    @BeforeEach
    void setUp() {
        chatInputValidator = new AssistantChatInputValidator();
        controller = new AssistantController(
                sendChatMessageUseCase,
                assistantProperties,
                assignmentRepository,
                chatContextFactory,
                chatInputValidator);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void chat_withJdPassesAuthContextToUseCase() {
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_JD"))));
        when(assistantProperties.isEnabled()).thenReturn(true);
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of());
        when(sendChatMessageUseCase.send(any(), any(), any(), any())).thenReturn(
                new AssistantChatResult("Respuesta del asistente.", "list_users", List.of("app_user"),
                        AssistantResolutionPath.KEYWORD, false));

        ResponseEntity<?> response = controller.chat(new SendChatMessageRequest("¿Qué usuarios tenemos?", null, null));

        assertThat(response.getStatusCode().value()).isEqualTo(200);

        ArgumentCaptor<AssistantAuthContext> authCaptor = ArgumentCaptor.forClass(AssistantAuthContext.class);
        verify(sendChatMessageUseCase).send(
                eq("¿Qué usuarios tenemos?"), any(), authCaptor.capture(), any());
        assertThat(authCaptor.getValue().role()).isEqualTo("JD");
        assertThat(authCaptor.getValue().userId()).isEqualTo(userId);
    }

    @Test
    void chat_withCcPassesCcRole() {
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CC"))));
        when(assistantProperties.isEnabled()).thenReturn(true);
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of());
        when(sendChatMessageUseCase.send(any(), any(), any(), any())).thenReturn(
                AssistantChatResult.outOfScope("No puedo listar usuarios."));

        controller.chat(new SendChatMessageRequest("Lista usuarios", null, null));

        ArgumentCaptor<AssistantAuthContext> authCaptor = ArgumentCaptor.forClass(AssistantAuthContext.class);
        verify(sendChatMessageUseCase).send(any(), any(), authCaptor.capture(), any());
        assertThat(authCaptor.getValue().role()).isEqualTo("CC");
    }

    @Test
    void status_withEvidenceAgentAndEe_throwsAccessDenied() {
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_EE"))));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of());

        assertThatThrownBy(() -> controller.getStatus("evidence"))
                .isInstanceOf(AssistantAgentAccessDeniedException.class)
                .hasMessageContaining("evidence");
    }

    @Test
    void chat_withEvidenceAgentAndEe_throwsAccessDenied() {
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_EE"))));
        when(assistantProperties.isEnabled()).thenReturn(true);
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of());

        assertThatThrownBy(() -> controller.chat(new SendChatMessageRequest(
                "Lista evidencias pendientes",
                null,
                new AssistantChatContextDto("evidence", null, null, null, null, null, null))))
                .isInstanceOf(AssistantAgentAccessDeniedException.class)
                .hasMessageContaining("evidence");
    }

    @Test
    void chat_rejectsSqlInjectionPayload() {
        when(assistantProperties.isEnabled()).thenReturn(true);

        assertThatThrownBy(() -> controller.chat(new SendChatMessageRequest(
                "SELECT * FROM app_user; DROP TABLE app_user",
                null,
                null)))
                .isInstanceOf(AssistantInvalidInputException.class);
    }
}
