package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAccessDeniedHandler;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.EvidenceExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.LinkDocumentAssetToIndicatorUseCase;
import com.umss.sigesa.application.port.in.ListNormativeIndicatorEvidencesUseCase;
import com.umss.sigesa.application.port.in.UploadNormativeIndicatorEvidenceUseCase;
import com.umss.sigesa.domain.exception.IndicatorAlreadyHasEvidenceException;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceUploadResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EvidenceController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        EvidenceExceptionHandler.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class EvidenceControllerWebMvcTest {

    private static final UUID CC_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UploadNormativeIndicatorEvidenceUseCase uploadUseCase;
    @MockitoBean
    private LinkDocumentAssetToIndicatorUseCase linkUseCase;
    @MockitoBean
    private ListNormativeIndicatorEvidencesUseCase listUseCase;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    void upload_returns201WithAssetFields() throws Exception {
        UUID indicatorId = UUID.randomUUID();
        UUID evidenceId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        when(uploadUseCase.upload(any())).thenReturn(new NormativeIndicatorEvidenceUploadResult(
                evidenceId, 1, "abc12345deadbeef", "EvidenceUploaded", IndicatorState.SUBIDO,
                assetId, "SIGESA-20261013-abc12345", false));

        mockMvc.perform(multipart("/api/v1/indicators/{indicatorId}/evidences", indicatorId)
                        .file(new MockMultipartFile("file", "doc.pdf", "application/pdf", "%PDF-1.4".getBytes()))
                        .param("description", "Plan de estudios")
                        .with(ccAuth()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.evidenceId").value(evidenceId.toString()))
                .andExpect(jsonPath("$.documentAssetId").value(assetId.toString()))
                .andExpect(jsonPath("$.folioCode").value("SIGESA-20261013-abc12345"))
                .andExpect(jsonPath("$.reusedAsset").value(false))
                .andExpect(jsonPath("$.currentState").value("SUBIDO"));
    }

    @Test
    void link_returns201Reused() throws Exception {
        UUID indicatorId = UUID.randomUUID();
        UUID evidenceId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        when(linkUseCase.link(any())).thenReturn(new NormativeIndicatorEvidenceUploadResult(
                evidenceId, 1, "abc12345deadbeef", "EvidenceUploaded", IndicatorState.SUBIDO,
                assetId, "SIGESA-20261013-abc12345", true));

        mockMvc.perform(post("/api/v1/indicators/{indicatorId}/evidences/link", indicatorId)
                        .with(ccAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documentAssetId\":\"%s\"}".formatted(assetId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reusedAsset").value(true))
                .andExpect(jsonPath("$.folioCode").value("SIGESA-20261013-abc12345"));
    }

    @Test
    void upload_mapsAlreadyHasEvidenceTo409() throws Exception {
        UUID indicatorId = UUID.randomUUID();
        when(uploadUseCase.upload(any())).thenThrow(new IndicatorAlreadyHasEvidenceException(indicatorId));

        mockMvc.perform(multipart("/api/v1/indicators/{indicatorId}/evidences", indicatorId)
                        .file(new MockMultipartFile("file", "doc.pdf", "application/pdf", "%PDF-1.4".getBytes()))
                        .param("description", "Plan")
                        .with(ccAuth()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INDICATOR_ALREADY_HAS_EVIDENCE"));
    }

    private static RequestPostProcessor ccAuth() {
        return authentication(new UsernamePasswordAuthenticationToken(
                CC_USER_ID,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CC"))));
    }
}
