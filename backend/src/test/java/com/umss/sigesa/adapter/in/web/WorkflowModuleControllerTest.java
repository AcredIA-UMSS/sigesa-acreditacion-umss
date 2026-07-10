package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.AuthExceptionHandler;
import com.umss.sigesa.adapter.in.web.advice.ProcessExceptionHandler;
import com.umss.sigesa.adapter.in.web.advice.WorkflowExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.ApproveIndicatorUseCase;
import com.umss.sigesa.application.port.in.ListEvidenceVersionsUseCase;
import com.umss.sigesa.application.port.in.ListIndicatorsUseCase;
import com.umss.sigesa.application.port.in.RejectIndicatorUseCase;
import com.umss.sigesa.application.port.in.SearchEvidencesUseCase;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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

@WebMvcTest(controllers = {
        IndicatorCatalogController.class,
        IndicatorWorkflowController.class,
        EvidenceQueryController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        AuthExceptionHandler.class,
        WorkflowExceptionHandler.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class WorkflowModuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListIndicatorsUseCase listIndicatorsUseCase;
    @MockitoBean
    private ApproveIndicatorUseCase approveIndicatorUseCase;
    @MockitoBean
    private RejectIndicatorUseCase rejectIndicatorUseCase;
    @MockitoBean
    private SearchEvidencesUseCase searchEvidencesUseCase;
    @MockitoBean
    private ListEvidenceVersionsUseCase listEvidenceVersionsUseCase;
    @MockitoBean
    private WebIdentityResolver identityResolver;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    private final AuthenticatedIdentity tdIdentity = new AuthenticatedIdentity(
            UUID.randomUUID(),
            Email.of("td@umss.edu.bo"),
            Role.TD,
            List.of()
    );

    @BeforeEach
    void setUpIdentity() {
        when(identityResolver.requireIdentity()).thenReturn(tdIdentity);
        when(identityResolver.programScopeForCurrentUser()).thenReturn(List.of());
    }

    @Test
    @WithMockUser(roles = "CC")
    void approveIndicator_asCoordinatorReturns403() throws Exception {
        mockMvc.perform(post("/api/v1/indicators/{id}/approve", UUID.randomUUID())
                        .with(user("testcc").roles("CC")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TD")
    void approveIndicator_asTechnicianReturns200() throws Exception {
        UUID indicatorId = UUID.randomUUID();
        UUID historyId = UUID.randomUUID();
        when(approveIndicatorUseCase.approve(eq(indicatorId), any()))
                .thenReturn(new ApproveIndicatorUseCase.ApproveIndicatorResult("APROBADO", historyId, "IndicatorApproved"));

        mockMvc.perform(post("/api/v1/indicators/{id}/approve", indicatorId)
                        .with(user("testtd").roles("TD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newState").value("APROBADO"))
                .andExpect(jsonPath("$.event").value("IndicatorApproved"));
    }

    @Test
    @WithMockUser(roles = "TD")
    void searchEvidences_asTechnicianReturns200() throws Exception {
        UUID evidenceId = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();
        when(searchEvidencesUseCase.search(any())).thenReturn(
                new SearchEvidencesUseCase.EvidenceSearchPage(
                        List.of(new SearchEvidencesUseCase.EvidenceSearchItem(
                                evidenceId,
                                indicatorId,
                                "IND-3.1.2",
                                "Infraestructura",
                                UUID.randomUUID(),
                                2,
                                1,
                                "Descripción",
                                LocalDateTime.now()
                        )),
                        0,
                        10,
                        1,
                        1
                )
        );

        mockMvc.perform(get("/api/v1/evidences/search")
                        .with(user("testtd").roles("TD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].evidenceId").value(evidenceId.toString()));
    }

    @Test
    @WithMockUser(roles = "JD")
    void searchEvidences_asJdReturns403() throws Exception {
        mockMvc.perform(get("/api/v1/evidences/search")
                        .with(user("testjd").roles("JD")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CC")
    void listIndicators_asCoordinatorReturns200() throws Exception {
        UUID indicatorId = UUID.randomUUID();
        when(listIndicatorsUseCase.list(any(), any(), any())).thenReturn(List.of(
                new ListIndicatorsUseCase.IndicatorSummary(
                        indicatorId,
                        "IND-3.1.2",
                        "Infraestructura",
                        UUID.randomUUID(),
                        2,
                        UUID.randomUUID(),
                        "SUBIDO"
                )
        ));

        mockMvc.perform(get("/api/v1/indicators")
                        .with(user("testcc").roles("CC")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(indicatorId.toString()))
                .andExpect(jsonPath("$[0].currentState").value("SUBIDO"));
    }

    @Test
    @WithMockUser(roles = "TD")
    void rejectIndicator_asTechnicianReturns200() throws Exception {
        UUID indicatorId = UUID.randomUUID();
        UUID historyId = UUID.randomUUID();
        when(rejectIndicatorUseCase.reject(eq(indicatorId), any(), any()))
                .thenReturn(new RejectIndicatorUseCase.RejectIndicatorResult("OBSERVADO", "OBS-123", historyId));

        mockMvc.perform(post("/api/v1/indicators/{id}/reject", indicatorId)
                        .with(user("testtd").roles("TD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"justification":"La evidencia no cumple con el criterio mínimo exigido por la norma."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newState").value("OBSERVADO"))
                .andExpect(jsonPath("$.observationId").value("OBS-123"));
    }
}
