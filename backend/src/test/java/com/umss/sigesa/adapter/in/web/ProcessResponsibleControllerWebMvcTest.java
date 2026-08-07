package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.ProcessExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.model.process.ProcessResponsibleInfo;
import com.umss.sigesa.application.port.in.AssignProcessResponsibleUseCase;
import com.umss.sigesa.application.port.in.ListEligibleResponsiblesUseCase;
import com.umss.sigesa.application.port.in.RemoveProcessResponsibleUseCase;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProcessResponsibleController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class, ProcessExceptionHandler.class})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class ProcessResponsibleControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssignProcessResponsibleUseCase assignProcessResponsibleUseCase;
    @MockitoBean
    private RemoveProcessResponsibleUseCase removeProcessResponsibleUseCase;
    @MockitoBean
    private ListEligibleResponsiblesUseCase listEligibleResponsiblesUseCase;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    @WithMockUser(roles = "JD")
    void jdCanAssignResponsible() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(assignProcessResponsibleUseCase.assign(eq(processId), eq(userId), any(UUID.class)))
                .thenReturn(new ProcessResponsibleInfo(
                        userId, "María Coordinadora", "cc@umss.edu.bo", LocalDateTime.now()));

        mockMvc.perform(put("/api/v1/processes/{processId}/responsible", processId)
                        .with(user("testjd").roles("JD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"%s"}
                                """.formatted(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("María Coordinadora"));
    }

    @Test
    @WithMockUser(roles = "CC")
    void ccCannotAssignResponsible() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/processes/{processId}/responsible", processId)
                        .with(user("testcc").roles("CC"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"%s"}
                                """.formatted(userId)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "JD")
    void jdCanListCandidates() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(listEligibleResponsiblesUseCase.listEligible(processId))
                .thenReturn(List.of(new ListEligibleResponsiblesUseCase.EligibleResponsible(
                        userId, "María Coordinadora", "cc@umss.edu.bo")));

        mockMvc.perform(get("/api/v1/processes/{processId}/responsible/candidates", processId)
                        .with(user("testjd").roles("JD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("cc@umss.edu.bo"));
    }

    @Test
    @WithMockUser(roles = "JD")
    void jdCanRemoveResponsible() throws Exception {
        UUID processId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/processes/{processId}/responsible", processId)
                        .with(user("testjd").roles("JD")))
                .andExpect(status().isNoContent());
    }
}
