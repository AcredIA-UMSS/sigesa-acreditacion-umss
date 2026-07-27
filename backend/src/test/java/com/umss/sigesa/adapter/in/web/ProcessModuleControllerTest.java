package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.AuthExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.ActivateTemplateUseCase;
import com.umss.sigesa.application.port.in.CreateProcessUseCase;
import com.umss.sigesa.application.port.in.GetProcessUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.in.ListTemplatesUseCase;
import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.ProcessType;
import com.umss.sigesa.domain.model.Taxonomy;
import com.umss.sigesa.domain.model.Template;
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

@WebMvcTest(controllers = {TemplateController.class, ProcessController.class})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        AuthExceptionHandler.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class ProcessModuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListTemplatesUseCase listTemplatesUseCase;
    @MockitoBean
    private ActivateTemplateUseCase activateTemplateUseCase;
    @MockitoBean
    private ListProcessesUseCase listProcessesUseCase;
    @MockitoBean
    private CreateProcessUseCase createProcessUseCase;
    @MockitoBean
    private GetProcessUseCase getProcessUseCase;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    @WithMockUser(roles = "CC")
    void listTemplates_asCoordinatorReturns403() throws Exception {
        mockMvc.perform(get("/api/v1/templates")
                        .with(user("testcc").roles("CC")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "JD")
    void listTemplates_asJdReturns200() throws Exception {
        UUID templateId = UUID.randomUUID();
        when(listTemplatesUseCase.list()).thenReturn(List.of(
                new ListTemplatesUseCase.TemplateSummary(
                        templateId,
                        true,
                        "CEUB-2026.1",
                        "2026-1",
                        LocalDateTime.now(),
                        ProcessType.CEUB
                )
        ));

        mockMvc.perform(get("/api/v1/templates")
                        .with(user("testjd").roles("JD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(templateId.toString()))
                .andExpect(jsonPath("$[0].validated").value(true))
                .andExpect(jsonPath("$[0].type").value("CEUB"));
    }

    @Test
    @WithMockUser(roles = "JD")
    void activateTemplate_asJdReturns200() throws Exception {
        UUID templateId = UUID.randomUUID();
        LocalDateTime activatedAt = LocalDateTime.of(2026, 1, 15, 10, 0);
        when(activateTemplateUseCase.activate(eq(templateId), eq("2026-1")))
                .thenReturn(new Template(templateId, true, new Taxonomy("CEUB-2026.1"), "2026-1", activatedAt));

        mockMvc.perform(post("/api/v1/templates/{id}/activate", templateId)
                        .with(user("testjd").roles("JD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"period\":\"2026-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateId").value(templateId.toString()))
                .andExpect(jsonPath("$.activePeriod").value("2026-1"));
    }

    @Test
    @WithMockUser(roles = "JD")
    void listProcesses_asJdReturns200() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 15, 10, 0);
        when(listProcessesUseCase.list(null, null, null)).thenReturn(List.of(
                new ListProcessesUseCase.ProcessSummary(
                        processId,
                        templateId,
                        careerId,
                        "2026-1",
                        ProcessType.CEUB,
                        ProcessStatus.ACTIVE,
                        "CEUB-2026.1",
                        createdAt
                )
        ));

        mockMvc.perform(get("/api/v1/processes")
                        .with(user("testjd").roles("JD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].processId").value(processId.toString()))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "JD")
    void getProcessById_asJdReturns200() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 15, 10, 0);
        when(getProcessUseCase.getById(processId)).thenReturn(java.util.Optional.of(
                new GetProcessUseCase.ProcessDetail(
                        processId,
                        templateId,
                        careerId,
                        "2026-1",
                        ProcessType.CEUB,
                        ProcessStatus.ACTIVE,
                        "CEUB-2026.1",
                        createdAt
                )
        ));

        mockMvc.perform(get("/api/v1/processes/{id}", processId)
                        .with(user("testjd").roles("JD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processId").value(processId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "TD")
    void createProcess_asTechnicianReturns403() throws Exception {
        mockMvc.perform(post("/api/v1/processes")
                        .with(user("testtd").roles("TD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "templateId":"850e8400-e29b-41d4-a716-446655440010",
                                  "careerId":"660e8400-e29b-41d4-a716-446655440001"
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
