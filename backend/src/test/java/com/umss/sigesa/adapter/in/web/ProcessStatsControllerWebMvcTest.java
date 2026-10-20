package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAccessDeniedHandler;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.ProcessExceptionHandler;
import com.umss.sigesa.adapter.in.web.mapper.NormativeStructureWebMapper;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.model.process.ProcessStats;
import com.umss.sigesa.application.port.in.CreateProcessUseCase;
import com.umss.sigesa.application.port.in.DeleteProcessUseCase;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.GetProcessStatsUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
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
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProcessController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        ProcessExceptionHandler.class,
        NormativeStructureWebMapper.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class ProcessStatsControllerWebMvcTest {

    private static final UUID TD_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateProcessUseCase createProcessUseCase;
    @MockitoBean
    private ListProcessesUseCase listProcessesUseCase;
    @MockitoBean
    private GetProcessStatsUseCase getProcessStatsUseCase;
    @MockitoBean
    private GetProcessDetailUseCase getProcessDetailUseCase;
    @MockitoBean
    private DeleteProcessUseCase deleteProcessUseCase;
    @MockitoBean
    private UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    void statsWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/processes/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void statsTdReturns200() throws Exception {
        when(userProgramAssignmentRepositoryPort.findActiveByUserId(any())).thenReturn(List.of());
        when(getProcessStatsUseCase.stats(any())).thenReturn(new ProcessStats(
                1, Map.of("ACTIVE", 1L), Map.of("ACTIVE", 1L), 0));

        mockMvc.perform(get("/api/v1/processes/stats").with(tdAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));
    }

    private static RequestPostProcessor tdAuth() {
        return authentication(new UsernamePasswordAuthenticationToken(
                TD_USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_TD"))));
    }
}
