package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.AssistantExceptionHandler;
import com.umss.sigesa.adapter.in.web.advice.AuthExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.model.assistant.AssistantChatResult;
import com.umss.sigesa.application.model.assistant.AssistantResolutionPath;
import com.umss.sigesa.application.model.assistant.AssistantToolStep;
import com.umss.sigesa.application.port.in.SendChatMessageUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.assistant.AssistantChatContextFactory;
import com.umss.sigesa.application.service.assistant.AssistantChatInputValidator;
import com.umss.sigesa.config.AssistantProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AssistantController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        AuthExceptionHandler.class,
        AssistantExceptionHandler.class,
        AssistantControllerWebMvcTest.TestConfig.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class AssistantControllerWebMvcTest {

    private static final UUID JD_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID EE_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    private static final String LIST_PROCESS_PHASES_ID = "list_process_phases";
    private static final String LIST_PROCESS_STRUCTURE_ID = "list_process_structure";
    private static final String SEARCH_NORMATIVE_DOCS_ID = "search_normative_docs";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SendChatMessageUseCase sendChatMessageUseCase;

    @MockitoBean
    private AssistantProperties assistantProperties;

    @MockitoBean
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    @MockitoBean
    private AssistantChatContextFactory chatContextFactory;

    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    void chat_withJdValidMessage_returns200AndResponseFields() throws Exception {
        stubAssistantEnabled();
        when(assignmentRepository.findActiveByUserId(JD_USER_ID)).thenReturn(List.of());
        when(sendChatMessageUseCase.send(any(), any(), any(), any())).thenReturn(
                new AssistantChatResult(
                        "Fases del proceso **Ingeniería de Sistemas**",
                        LIST_PROCESS_STRUCTURE_ID,
                        List.of("level1_nodes"),
                        AssistantResolutionPath.KEYWORD,
                        false));

        mockMvc.perform(post("/api/v1/assistant/chat")
                        .with(jdAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Lista las fases de Ingeniería de Sistemas CEUB"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").isNotEmpty())
                .andExpect(jsonPath("$.toolId").value(LIST_PROCESS_STRUCTURE_ID))
                .andExpect(jsonPath("$.path").value("KEYWORD"))
                .andExpect(jsonPath("$.llmInvoked").value(false))
                .andExpect(jsonPath("$.sourceTables[0]").value("level1_nodes"));
    }

    @Test
    void chat_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Lista las fases"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void chat_withSqlInjectionPayload_returns400() throws Exception {
        stubAssistantEnabled();
        when(assignmentRepository.findActiveByUserId(JD_USER_ID)).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/assistant/chat")
                        .with(jdAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"SELECT * FROM app_user; DROP TABLE app_user"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ASSISTANT_INVALID_INPUT"));
    }

    @Test
    void chat_withEvidenceAgentAndEe_returns403() throws Exception {
        stubAssistantEnabled();
        when(assignmentRepository.findActiveByUserId(EE_USER_ID)).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/assistant/chat")
                        .with(eeAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message":"Lista evidencias pendientes",
                                  "context":{"agent":"evidence","programId":"950e8400-e29b-41d4-a716-446655440020"}
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
    }

    @Test
    void status_withPhasesAgent_returns200AndScenarios() throws Exception {
        stubAssistantEnabled();
        when(assignmentRepository.findActiveByUserId(JD_USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/assistant/status")
                        .param("agent", "phases")
                        .with(jdAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.agent").value("phases"))
                .andExpect(jsonPath("$.demoScenarios").isArray())
                .andExpect(jsonPath("$.demoScenarios.length()").value(5))
                .andExpect(jsonPath("$.capabilities").isArray());
    }

    @Test
    void status_withEvidenceAgentAndEe_returns403() throws Exception {
        when(assignmentRepository.findActiveByUserId(EE_USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/assistant/status")
                        .param("agent", "evidence")
                        .with(eeAuth()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
    }

    @Test
    void chat_responseContract_includesReplyPathAndSequentialSteps() throws Exception {
        stubAssistantEnabled();
        when(assignmentRepository.findActiveByUserId(JD_USER_ID)).thenReturn(List.of());
        when(sendChatMessageUseCase.send(any(), any(), any(), any())).thenReturn(
                AssistantChatResult.fromSteps(
                        "Paso 1 completado.",
                        AssistantResolutionPath.LLM,
                        true,
                        List.of(
                                new AssistantToolStep(
                                        1,
                                        LIST_PROCESS_STRUCTURE_ID,
                                        List.of("phases", "subphases"),
                                        true),
                                new AssistantToolStep(
                                        2,
                                        SEARCH_NORMATIVE_DOCS_ID,
                                        List.of("normative_documents"),
                                        true))));

        mockMvc.perform(post("/api/v1/assistant/chat")
                        .with(jdAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Muestra estructura y busca normativa CEUB"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Paso 1 completado."))
                .andExpect(jsonPath("$.path").value("LLM"))
                .andExpect(jsonPath("$.llmInvoked").value(true))
                .andExpect(jsonPath("$.steps.length()").value(2))
                .andExpect(jsonPath("$.steps[0].step").value(1))
                .andExpect(jsonPath("$.steps[0].toolId").value(LIST_PROCESS_STRUCTURE_ID))
                .andExpect(jsonPath("$.steps[0].success").value(true))
                .andExpect(jsonPath("$.steps[1].step").value(2))
                .andExpect(jsonPath("$.steps[1].toolId").value(SEARCH_NORMATIVE_DOCS_ID));
    }

    @Test
    void chat_whenAssistantDisabled_returns503() throws Exception {
        when(assistantProperties.isEnabled()).thenReturn(false);
        when(assignmentRepository.findActiveByUserId(JD_USER_ID)).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/assistant/chat")
                        .with(jdAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Lista las fases"}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("ASSISTANT_UNAVAILABLE"));
    }

    private static RequestPostProcessor jdAuth() {
        return authentication(new UsernamePasswordAuthenticationToken(
                JD_USER_ID,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_JD"))));
    }

    private static RequestPostProcessor eeAuth() {
        return authentication(new UsernamePasswordAuthenticationToken(
                EE_USER_ID,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_EE"))));
    }

    private void stubAssistantEnabled() {
        when(assistantProperties.isEnabled()).thenReturn(true);
        when(assistantProperties.isLlmEnabled()).thenReturn(true);
        when(assistantProperties.getModel()).thenReturn("test-model");
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        AssistantChatInputValidator assistantChatInputValidator() {
            return new AssistantChatInputValidator();
        }
    }
}
