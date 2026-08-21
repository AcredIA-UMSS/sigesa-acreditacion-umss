package com.umss.sigesa.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.EvidenceExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.ApproveIndicatorUseCase;
import com.umss.sigesa.application.port.in.ApproveIndicatorUseCase.ApproveResult;
import com.umss.sigesa.application.port.in.RejectIndicatorUseCase;
import com.umss.sigesa.application.port.in.RejectIndicatorUseCase.RejectResult;
import com.umss.sigesa.application.port.out.EvidenceControlQueryPort;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.InvalidIndicatorStateException;
import com.umss.sigesa.domain.exception.JustificationRequiredException;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IndicatorWorkflowController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class, EvidenceExceptionHandler.class})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class IndicatorWorkflowControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ApproveIndicatorUseCase approveIndicatorUseCase;

    @MockitoBean
    private RejectIndicatorUseCase rejectIndicatorUseCase;

    @MockitoBean
    private EvidenceControlQueryPort evidenceControlQueryPort;

    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    @DisplayName("GET /pending - Retorna 200 OK para usuario con rol TD")
    void listPending_returns200ForTD() throws Exception {
        when(evidenceControlQueryPort.listByProgramIdsAndStates(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/indicators/pending")
                        .with(user(UUID.randomUUID().toString()).roles("TD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /pending - Retorna 403 Forbidden para usuario con rol CC")
    void listPending_returns403ForCC() throws Exception {
        mockMvc.perform(get("/api/v1/indicators/pending")
                        .with(user(UUID.randomUUID().toString()).roles("CC")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /approve - Retorna 200 OK en aprobación exitosa por el TD")
    void approve_returns200ForTD() throws Exception {
        UUID indicatorId = UUID.randomUUID();
        UUID historyId = UUID.randomUUID();
        when(approveIndicatorUseCase.approve(eq(indicatorId), any(), eq(Role.TD)))
                .thenReturn(new ApproveResult(IndicatorState.APROBADO, historyId, "IndicatorApproved"));

        mockMvc.perform(post("/api/v1/indicators/{indicatorId}/approve", indicatorId)
                        .with(user(UUID.randomUUID().toString()).roles("TD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newState").value("APROBADO"))
                .andExpect(jsonPath("$.stateHistoryId").value(historyId.toString()))
                .andExpect(jsonPath("$.event").value("IndicatorApproved"));
    }

    @Test
    @DisplayName("POST /approve - Retorna 409 Conflict cuando el estado es inválido")
    void approve_returns409WhenInvalidState() throws Exception {
        UUID indicatorId = UUID.randomUUID();
        when(approveIndicatorUseCase.approve(eq(indicatorId), any(), any()))
                .thenThrow(new InvalidIndicatorStateException("El indicador no se encuentra en estado SUBIDO"));

        mockMvc.perform(post("/api/v1/indicators/{indicatorId}/approve", indicatorId)
                        .with(user(UUID.randomUUID().toString()).roles("TD")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INVALID_STATE"));
    }

    @Test
    @DisplayName("POST /approve - Retorna 404 Not Found cuando el indicador no existe")
    void approve_returns404WhenNotFound() throws Exception {
        UUID indicatorId = UUID.randomUUID();
        when(approveIndicatorUseCase.approve(eq(indicatorId), any(), any()))
                .thenThrow(new IndicatorNotFoundException(indicatorId));

        mockMvc.perform(post("/api/v1/indicators/{indicatorId}/approve", indicatorId)
                        .with(user(UUID.randomUUID().toString()).roles("TD")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("INDICATOR_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /reject - Retorna 200 OK en rechazo exitoso con justificación adecuada")
    void reject_returns200ForTDWithValidJustification() throws Exception {
        UUID indicatorId = UUID.randomUUID();
        UUID historyId = UUID.randomUUID();
        String justification = "Justificación válida con mas de 20 caracteres";
        IndicatorWorkflowController.RejectRequestDto body = new IndicatorWorkflowController.RejectRequestDto(justification);

        when(rejectIndicatorUseCase.reject(eq(indicatorId), eq(justification), any(), eq(Role.TD)))
                .thenReturn(new RejectResult(IndicatorState.OBSERVADO, "OBS-12345678", historyId));

        mockMvc.perform(post("/api/v1/indicators/{indicatorId}/reject", indicatorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(user(UUID.randomUUID().toString()).roles("TD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newState").value("OBSERVADO"))
                .andExpect(jsonPath("$.observationId").value("OBS-12345678"))
                .andExpect(jsonPath("$.stateHistoryId").value(historyId.toString()));
    }

    @Test
    @DisplayName("POST /reject - Retorna 422 Unprocessable Entity si la justificación es corta")
    void reject_returns422WhenJustificationTooShort() throws Exception {
        UUID indicatorId = UUID.randomUUID();
        String justification = "Demasiado corta";
        IndicatorWorkflowController.RejectRequestDto body = new IndicatorWorkflowController.RejectRequestDto(justification);

        when(rejectIndicatorUseCase.reject(eq(indicatorId), eq(justification), any(), any()))
                .thenThrow(new JustificationRequiredException("La justificación debe tener al menos 20 caracteres"));

        mockMvc.perform(post("/api/v1/indicators/{indicatorId}/reject", indicatorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(user(UUID.randomUUID().toString()).roles("TD")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("JUSTIFICATION_REQUIRED"));
    }
}
