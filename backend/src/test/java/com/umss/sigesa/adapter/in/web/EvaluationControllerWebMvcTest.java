package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAccessDeniedHandler;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.EvaluationExceptionHandler;
import com.umss.sigesa.adapter.in.web.advice.EvidenceExceptionHandler;
import com.umss.sigesa.adapter.in.web.advice.ProcessExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.GetEvaluationMetricsUseCase;
import com.umss.sigesa.application.port.in.GetIndicatorAnalysisUseCase;
import com.umss.sigesa.application.port.in.ListPrimarySurveyBatchesUseCase;
import com.umss.sigesa.application.port.in.RegisterPrimarySurveyBatchUseCase;
import com.umss.sigesa.application.port.in.UpsertIndicatorAnalysisUseCase;
import com.umss.sigesa.domain.exception.IndicatorAnalysisNotFoundException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.model.ComplianceJudgment;
import com.umss.sigesa.domain.model.EvaluationMetrics;
import com.umss.sigesa.domain.model.FodaQuadrant;
import com.umss.sigesa.domain.model.IndicatorAnalysis;
import com.umss.sigesa.domain.model.PrimarySurveyBatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        EvaluationMetricsController.class,
        IndicatorAnalysisController.class,
        SurveyBatchController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        EvaluationExceptionHandler.class,
        EvidenceExceptionHandler.class,
        ProcessExceptionHandler.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class EvaluationControllerWebMvcTest {

    private static final UUID CC_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TD_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetEvaluationMetricsUseCase getEvaluationMetricsUseCase;
    @MockitoBean
    private GetIndicatorAnalysisUseCase getIndicatorAnalysisUseCase;
    @MockitoBean
    private UpsertIndicatorAnalysisUseCase upsertIndicatorAnalysisUseCase;
    @MockitoBean
    private RegisterPrimarySurveyBatchUseCase registerPrimarySurveyBatchUseCase;
    @MockitoBean
    private ListPrimarySurveyBatchesUseCase listPrimarySurveyBatchesUseCase;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    void getMetrics_returns200() throws Exception {
        UUID processId = UUID.randomUUID();
        when(getEvaluationMetricsUseCase.get(eq(processId), eq(TD_USER_ID), any(), any()))
                .thenReturn(EvaluationMetrics.of(processId, 10, 10, 4, 1));

        mockMvc.perform(get("/api/v1/processes/{processId}/evaluation-metrics", processId).with(tdAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIndicators").value(10))
                .andExpect(jsonPath("$.fodaComplete").value(4))
                .andExpect(jsonPath("$.primarySurveyBatchCount").value(1));
    }

    @Test
    void putAnalysis_returns200() throws Exception {
        UUID indicatorId = UUID.randomUUID();
        when(upsertIndicatorAnalysisUseCase.upsert(any())).thenReturn(IndicatorAnalysis.builder()
                .id(UUID.randomUUID())
                .indicatorId(indicatorId)
                .processId(UUID.randomUUID())
                .fodaQuadrant(FodaQuadrant.WEAKNESS)
                .compliance(ComplianceJudgment.PARTIAL)
                .synthesis("Síntesis")
                .authorId(CC_USER_ID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        mockMvc.perform(put("/api/v1/indicators/{indicatorId}/analysis", indicatorId)
                        .with(ccAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fodaQuadrant":"WEAKNESS","compliance":"PARTIAL","synthesis":"Síntesis"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fodaQuadrant").value("WEAKNESS"))
                .andExpect(jsonPath("$.compliance").value("PARTIAL"));
    }

    @Test
    void putAnalysis_tdForbidden() throws Exception {
        mockMvc.perform(put("/api/v1/indicators/{indicatorId}/analysis", UUID.randomUUID())
                        .with(tdAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fodaQuadrant":"WEAKNESS","compliance":"PARTIAL","synthesis":"Síntesis"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAnalysis_notFound() throws Exception {
        UUID indicatorId = UUID.randomUUID();
        when(getIndicatorAnalysisUseCase.get(eq(indicatorId), eq(TD_USER_ID), any(), any()))
                .thenThrow(new IndicatorAnalysisNotFoundException(indicatorId));

        mockMvc.perform(get("/api/v1/indicators/{indicatorId}/analysis", indicatorId).with(tdAuth()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ANALYSIS_NOT_FOUND"));
    }

    @Test
    void postSurveyBatch_returns201() throws Exception {
        UUID processId = UUID.randomUUID();
        when(registerPrimarySurveyBatchUseCase.register(any())).thenReturn(PrimarySurveyBatch.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .audience("ESTUDIANTES")
                .responsesCount(120)
                .registeredAt(LocalDateTime.now())
                .build());

        mockMvc.perform(post("/api/v1/processes/{processId}/survey-batches", processId)
                        .with(ccAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"audience":"ESTUDIANTES","responsesCount":120}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.audience").value("ESTUDIANTES"))
                .andExpect(jsonPath("$.responsesCount").value(120));
    }

    @Test
    void postSurveyBatch_assetOtherProcessForbidden() throws Exception {
        UUID processId = UUID.randomUUID();
        when(registerPrimarySurveyBatchUseCase.register(any())).thenThrow(new ProgramScopeDeniedException());

        mockMvc.perform(post("/api/v1/processes/{processId}/survey-batches", processId)
                        .with(ccAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"audience":"ESTUDIANTES","responsesCount":10,"documentAssetId":"33333333-3333-3333-3333-333333333333"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("PROGRAM_SCOPE_DENIED"));
    }

    private static RequestPostProcessor ccAuth() {
        return authentication(new UsernamePasswordAuthenticationToken(
                CC_USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_CC"))));
    }

    private static RequestPostProcessor tdAuth() {
        return authentication(new UsernamePasswordAuthenticationToken(
                TD_USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_TD"))));
    }
}
