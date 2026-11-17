package com.umss.sigesa.redteam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAccessDeniedHandler;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.AssistantController;
import com.umss.sigesa.adapter.in.web.advice.AssistantExceptionHandler;
import com.umss.sigesa.adapter.in.web.advice.AuthExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantChatContext;
import com.umss.sigesa.application.model.assistant.AssistantChatResult;
import com.umss.sigesa.application.model.assistant.AssistantResolutionPath;
import com.umss.sigesa.application.port.in.SendChatMessageUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.assistant.AssistantChatContextFactory;
import com.umss.sigesa.application.service.assistant.AssistantChatInputValidator;
import com.umss.sigesa.application.service.assistant.AssistantReplyOutputGuard;
import com.umss.sigesa.config.AssistantProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Red Team — catálogo sincronizado desde tools/red-team-agent (./run.sh sync).
 * <p>
 * Guardrail: HTTP 400 real vía {@link AssistantChatInputValidator}.
 * Semántico (LLM): use case mockeado; filtración real → {@code ./run.sh probar-api} con LLM vivo.
 */
@WebMvcTest(controllers = AssistantController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        AuthExceptionHandler.class,
        AssistantExceptionHandler.class,
        RedTeamAssistantCatalogWebMvcTest.TestConfig.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class RedTeamAssistantCatalogWebMvcTest {

    private static final UUID CC_USER_ID = UUID.fromString("702de2ff-30ea-44d9-ba02-0df406d2a682");
    private static final UUID JD_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private static final ObjectMapper JSON = new ObjectMapper();

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

    static Stream<Arguments> guardrailAttackArguments() {
        return RedTeamCatalogLoader.loadAttacks().stream()
                .filter(a -> a.expectHttpStatus() != null)
                .map(a -> Arguments.of(a.id(), a.title(), a));
    }

    static Stream<Arguments> semanticAttackArguments() {
        return RedTeamCatalogLoader.loadAttacks().stream()
                .filter(a -> a.expectHttpStatus() == null)
                .map(a -> Arguments.of(a.id(), a.title(), a));
    }

    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("guardrailAttackArguments")
    @DisplayName("Red Team guardrail — entrada bloqueada")
    void guardrailAttack_blockedAtValidator(String id, String title, RedTeamAttackRecord attack) throws Exception {
        stubAssistantEnabled();
        UUID userId = authUserId(attack);
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of());
        stubChatContext(attack);

        mockMvc.perform(post("/api/v1/assistant/chat")
                        .with(authFor(attack))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildChatBody(attack)))
                .andExpect(status().is(attack.expectHttpStatus()))
                .andExpect(jsonPath("$.error").value("ASSISTANT_INVALID_INPUT"));
    }

    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("semanticAttackArguments")
    @DisplayName("Red Team semántico — pasa validador; respuesta mock segura sin filtración")
    void semanticAttack_mockSafeReply_passesEvaluator(String id, String title, RedTeamAttackRecord attack)
            throws Exception {
        stubAssistantEnabled();
        UUID userId = authUserId(attack);
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of());
        stubChatContext(attack);
        when(sendChatMessageUseCase.send(any(), any(), any(), any()))
                .thenReturn(new AssistantChatResult(
                        "No puedo compartir códigos internos ni ignorar las políticas del asistente SIGESA.",
                        null,
                        List.of(),
                        AssistantResolutionPath.LLM,
                        true));

        var result = mockMvc.perform(post("/api/v1/assistant/chat")
                        .with(authFor(attack))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildChatBody(attack)))
                .andExpect(status().isOk())
                .andReturn();

        String reply = JSON.readTree(result.getResponse().getContentAsString()).get("reply").asText();
        assertThat(RedTeamReplyEvaluator.evaluateReply(attack, reply))
                .as("Red Team %s (%s)", id, title)
                .isEmpty();
    }

    private void stubAssistantEnabled() {
        when(assistantProperties.isEnabled()).thenReturn(true);
        when(assistantProperties.isLlmEnabled()).thenReturn(true);
        when(assistantProperties.getModel()).thenReturn("test-model");
    }

    private static UUID authUserId(RedTeamAttackRecord attack) {
        return "users".equals(attack.agent()) ? JD_USER_ID : CC_USER_ID;
    }

    private static RequestPostProcessor authFor(RedTeamAttackRecord attack) {
        if ("users".equals(attack.agent())) {
            return authentication(new UsernamePasswordAuthenticationToken(
                    JD_USER_ID,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_JD"))));
        }
        return authentication(new UsernamePasswordAuthenticationToken(
                CC_USER_ID,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CC"))));
    }

    private void stubChatContext(RedTeamAttackRecord attack) {
        if (attack.agent() == null || "general".equals(attack.agent())) {
            return;
        }
        when(chatContextFactory.resolve(any(), any(), any(), any(), any(AssistantAuthContext.class)))
                .thenReturn(AssistantChatContext.general());
    }

    private static String buildChatBody(RedTeamAttackRecord attack) throws Exception {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("message", attack.userMessage());
        body.put("history", attack.history() == null ? Collections.emptyList() : attack.history());
        body.put("context", Map.of("agent", attack.agent() == null ? "general" : attack.agent()));
        return JSON.writeValueAsString(body);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        AssistantChatInputValidator assistantChatInputValidator() {
            return new AssistantChatInputValidator();
        }

        @Bean
        AssistantReplyOutputGuard assistantReplyOutputGuard() {
            return new AssistantReplyOutputGuard(true);
        }
    }
}
