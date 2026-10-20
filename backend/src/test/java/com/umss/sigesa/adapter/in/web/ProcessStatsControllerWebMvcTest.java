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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
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
        ProcessExceptionHandler.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class ProcessStatsControllerWebMvcTest {

    private static final UUID JD_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

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
    private NormativeStructureWebMapper normativeStructureWebMapper;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    void stats_jdReturns200WithUseCaseJson() throws Exception {
        when(userProgramAssignmentRepositoryPort.findActiveByUserId(any())).thenReturn(List.of());
        when(getProcessStatsUseCase.getStats(any())).thenReturn(sampleStats());

        mockMvc.perform(get("/api/v1/processes/stats").with(jdAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.frozenCount").value(1))
                .andExpect(jsonPath("$.byStatus.ACTIVE").value(1))
                .andExpect(jsonPath("$.byStatus.CLOSED").value(1))
                .andExpect(jsonPath("$.byOperationalMode.ACTIVE").value(1))
                .andExpect(jsonPath("$.byOperationalMode.AUDIT_READONLY").value(1))
                .andExpect(jsonPath("$.byOperationalMode.IMPROVEMENT_MONITORING").value(0));
    }

    @Test
    void stats_withoutTokenReturns401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/processes/stats"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void stats_withRoleOutsideAllowedSetReturns403AccessDenied() throws Exception {
        mockMvc.perform(get("/api/v1/processes/stats").with(otherRoleAuth()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("No autorizado para esta operación"));

        verifyNoInteractions(getProcessStatsUseCase);
    }

    private static ProcessStats sampleStats() {
        Map<String, Integer> byStatus = new LinkedHashMap<>();
        byStatus.put("ACTIVE", 1);
        byStatus.put("CLOSED", 1);
        Map<String, Integer> byOperationalMode = new LinkedHashMap<>();
        byOperationalMode.put("ACTIVE", 1);
        byOperationalMode.put("AUDIT_READONLY", 1);
        byOperationalMode.put("IMPROVEMENT_MONITORING", 0);
        return new ProcessStats(2, byStatus, byOperationalMode, 1);
    }

    private static RequestPostProcessor jdAuth() {
        return authentication(new UsernamePasswordAuthenticationToken(
                JD_USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_JD"))));
    }

    private static RequestPostProcessor otherRoleAuth() {
        return authentication(new UsernamePasswordAuthenticationToken(
                JD_USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_OTHER"))));
    }
}
