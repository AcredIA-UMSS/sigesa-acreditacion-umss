package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAccessDeniedHandler;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.ProcessExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.ApproveStageDeliverableUseCase;
import com.umss.sigesa.application.port.in.ApproveStageUseCase;
import com.umss.sigesa.application.port.in.EvaluateStageGateUseCase;
import com.umss.sigesa.application.port.in.GenerateStageDeliverableDocumentUseCase;
import com.umss.sigesa.application.port.in.ListProcessStagesUseCase;
import com.umss.sigesa.application.port.in.ObserveStageUseCase;
import com.umss.sigesa.application.port.in.SubmitStageForReviewUseCase;
import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.domain.exception.DeliverableImmutableException;
import com.umss.sigesa.domain.model.GeneratedStageDeliverable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StageWorkflowController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        ProcessExceptionHandler.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class StageDeliverableGenerateWebMvcTest {

    private static final UUID CC_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TD_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListProcessStagesUseCase listProcessStagesUseCase;
    @MockitoBean
    private SubmitStageForReviewUseCase submitStageForReviewUseCase;
    @MockitoBean
    private ApproveStageUseCase approveStageUseCase;
    @MockitoBean
    private ObserveStageUseCase observeStageUseCase;
    @MockitoBean
    private EvaluateStageGateUseCase evaluateStageGateUseCase;
    @MockitoBean
    private ApproveStageDeliverableUseCase approveStageDeliverableUseCase;
    @MockitoBean
    private GenerateStageDeliverableDocumentUseCase generateStageDeliverableDocumentUseCase;
    @MockitoBean
    private DocumentAssetPort documentAssetPort;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    void generate_returns201() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID deliverableId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        when(generateStageDeliverableDocumentUseCase.generate(
                eq(processId), eq(stageId), eq(deliverableId), eq(CC_USER_ID), any(), any()))
                .thenReturn(new GeneratedStageDeliverable(
                        deliverableId, assetId, "SIGESA-20261013-abc12345", "Plan de Mejoras (borrador institucional)"));

        mockMvc.perform(post(
                        "/api/v1/processes/{processId}/stages/{stageId}/deliverables/{deliverableId}/generate",
                        processId, stageId, deliverableId)
                        .with(ccAuth()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deliverableId").value(deliverableId.toString()))
                .andExpect(jsonPath("$.documentAssetId").value(assetId.toString()))
                .andExpect(jsonPath("$.folioCode").value("SIGESA-20261013-abc12345"));
    }

    @Test
    void generate_approvedReturns409() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID deliverableId = UUID.randomUUID();
        when(generateStageDeliverableDocumentUseCase.generate(any(), any(), any(), any(), any(), any()))
                .thenThrow(new DeliverableImmutableException("No se puede regenerar un entregable E4 ya aprobado."));

        mockMvc.perform(post(
                        "/api/v1/processes/{processId}/stages/{stageId}/deliverables/{deliverableId}/generate",
                        processId, stageId, deliverableId)
                        .with(ccAuth()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DELIVERABLE_IMMUTABLE"));
    }

    @Test
    void generate_tdForbidden() throws Exception {
        mockMvc.perform(post(
                        "/api/v1/processes/{processId}/stages/{stageId}/deliverables/{deliverableId}/generate",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                        .with(tdAuth()))
                .andExpect(status().isForbidden());
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
