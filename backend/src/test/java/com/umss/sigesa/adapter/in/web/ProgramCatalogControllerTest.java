package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.AuthExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.ListProgramsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProgramCatalogController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class, AuthExceptionHandler.class})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class ProgramCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListProgramsUseCase listProgramsUseCase;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    void list_withoutAuthenticationReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/programs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser(roles = "JD")
    void list_withAuthenticationReturns200() throws Exception {
        UUID programId = UUID.randomUUID();
        when(listProgramsUseCase.list()).thenReturn(List.of(
                new ListProgramsUseCase.ProgramSummary(programId, "INF-SIS", "Ingeniería de Sistemas")
        ));

        mockMvc.perform(get("/api/v1/programs")
                        .with(user("testjd").roles("JD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(programId.toString()))
                .andExpect(jsonPath("$[0].code").value("INF-SIS"))
                .andExpect(jsonPath("$[0].name").value("Ingeniería de Sistemas"));
    }
}
