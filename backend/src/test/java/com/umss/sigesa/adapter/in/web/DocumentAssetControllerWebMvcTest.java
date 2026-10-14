package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAccessDeniedHandler;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.EvidenceExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.GetDocumentAssetUseCase;
import com.umss.sigesa.domain.exception.DocumentAssetNotFoundException;
import com.umss.sigesa.domain.model.DocumentAssetView;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DocumentAssetController.class)
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
class DocumentAssetControllerWebMvcTest {

    private static final UUID TD_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetDocumentAssetUseCase getDocumentAssetUseCase;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    void get_returnsAsset() throws Exception {
        UUID assetId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();
        when(getDocumentAssetUseCase.get(eq(assetId), eq(TD_USER_ID), any()))
                .thenReturn(new DocumentAssetView(
                        assetId,
                        "SIGESA-20261013-abcd1234",
                        "plan.pdf",
                        "abcd1234ffff",
                        processId,
                        List.of(indicatorId),
                        LocalDateTime.of(2026, 10, 13, 12, 0)));

        mockMvc.perform(get("/api/v1/document-assets/{id}", assetId).with(tdAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folioCode").value("SIGESA-20261013-abcd1234"))
                .andExpect(jsonPath("$.linkedIndicatorIds[0]").value(indicatorId.toString()));
    }

    @Test
    void get_notFound() throws Exception {
        UUID assetId = UUID.randomUUID();
        when(getDocumentAssetUseCase.get(eq(assetId), eq(TD_USER_ID), any()))
                .thenThrow(new DocumentAssetNotFoundException(assetId));

        mockMvc.perform(get("/api/v1/document-assets/{id}", assetId).with(tdAuth()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("DOCUMENT_ASSET_NOT_FOUND"));
    }

    private static RequestPostProcessor tdAuth() {
        return authentication(new UsernamePasswordAuthenticationToken(
                TD_USER_ID,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_TD"))));
    }
}
