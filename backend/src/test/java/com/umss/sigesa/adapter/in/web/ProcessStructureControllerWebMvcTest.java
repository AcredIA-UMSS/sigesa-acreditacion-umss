package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.ProcessExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.AddProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.AddProcessSubphaseUseCase;
import com.umss.sigesa.application.port.in.DeleteProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.DeleteProcessSubphaseUseCase;
import com.umss.sigesa.application.port.in.ReorderProcessStructureUseCase;
import com.umss.sigesa.application.port.in.UpdateProcessPhaseUseCase;
import com.umss.sigesa.application.port.in.UpdateProcessSubphaseUseCase;
import com.umss.sigesa.domain.model.Phase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProcessStructureController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class, ProcessExceptionHandler.class})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class ProcessStructureControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddProcessPhaseUseCase addProcessPhaseUseCase;
    @MockitoBean
    private UpdateProcessPhaseUseCase updateProcessPhaseUseCase;
    @MockitoBean
    private DeleteProcessPhaseUseCase deleteProcessPhaseUseCase;
    @MockitoBean
    private AddProcessSubphaseUseCase addProcessSubphaseUseCase;
    @MockitoBean
    private UpdateProcessSubphaseUseCase updateProcessSubphaseUseCase;
    @MockitoBean
    private DeleteProcessSubphaseUseCase deleteProcessSubphaseUseCase;
    @MockitoBean
    private ReorderProcessStructureUseCase reorderProcessStructureUseCase;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    @WithMockUser(roles = "JD")
    void shouldCreatePhaseForJd() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();

        when(addProcessPhaseUseCase.execute(eq(processId), eq("Nueva fase"), eq(2), eq("Desc")))
                .thenReturn(Phase.builder().id(phaseId).name("Nueva fase").order(2).description("Desc").build());

        mockMvc.perform(post("/api/v1/processes/{processId}/phases", processId)
                        .with(user("testjd").roles("JD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Nueva fase","order":2,"description":"Desc"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Nueva fase"))
                .andExpect(jsonPath("$.order").value(2));
    }

    @Test
    @WithMockUser(roles = "TD")
    void shouldCreatePhaseForTd() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();

        when(addProcessPhaseUseCase.execute(eq(processId), eq("Nueva fase"), eq(2), eq("Desc")))
                .thenReturn(Phase.builder().id(phaseId).name("Nueva fase").order(2).description("Desc").build());

        mockMvc.perform(post("/api/v1/processes/{processId}/phases", processId)
                        .with(user("testtd").roles("TD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Nueva fase","order":2,"description":"Desc"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Nueva fase"));
    }

    @Test
    @WithMockUser(roles = "CC")
    void shouldRejectCcMutations() throws Exception {
        UUID processId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/processes/{processId}/phases", processId)
                        .with(user("testcc").roles("CC"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Nueva fase","order":1}
                                """))
                .andExpect(status().isForbidden());
    }
}
