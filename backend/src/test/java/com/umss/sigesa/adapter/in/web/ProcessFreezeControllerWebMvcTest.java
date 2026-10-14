package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAccessDeniedHandler;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.ProcessExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.FreezeProcessUseCase;
import com.umss.sigesa.application.port.in.GetProcessFreezeUseCase;
import com.umss.sigesa.application.port.in.LiftProcessFreezeUseCase;
import com.umss.sigesa.domain.exception.AlreadyFrozenException;
import com.umss.sigesa.domain.model.ProcessFreeze;
import com.umss.sigesa.domain.model.ProcessFreezeMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProcessFreezeController.class)
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
class ProcessFreezeControllerWebMvcTest {

    private static final UUID TD_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID EE_USER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FreezeProcessUseCase freezeProcessUseCase;
    @MockitoBean
    private LiftProcessFreezeUseCase liftProcessFreezeUseCase;
    @MockitoBean
    private GetProcessFreezeUseCase getProcessFreezeUseCase;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    void freeze_tdReturns201() throws Exception {
        UUID processId = UUID.randomUUID();
        when(freezeProcessUseCase.freeze(eq(processId), eq(TD_USER_ID), any(), any()))
                .thenReturn(activeFreeze(processId));

        mockMvc.perform(post("/api/v1/processes/{processId}/freeze", processId).with(tdAuth()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mode").value("AUDIT_READONLY"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void freeze_alreadyFrozenReturns409() throws Exception {
        UUID processId = UUID.randomUUID();
        when(freezeProcessUseCase.freeze(eq(processId), eq(TD_USER_ID), any(), any()))
                .thenThrow(new AlreadyFrozenException());

        mockMvc.perform(post("/api/v1/processes/{processId}/freeze", processId).with(tdAuth()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ALREADY_FROZEN"));
    }

    @Test
    void freeze_eeForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/processes/{processId}/freeze", UUID.randomUUID()).with(eeAuth()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getFreeze_eeAllowed() throws Exception {
        UUID processId = UUID.randomUUID();
        when(getProcessFreezeUseCase.get(eq(processId), eq(EE_USER_ID), any(), any()))
                .thenReturn(activeFreeze(processId));

        mockMvc.perform(get("/api/v1/processes/{processId}/freeze", processId).with(eeAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void lift_tdReturns200() throws Exception {
        UUID processId = UUID.randomUUID();
        ProcessFreeze lifted = ProcessFreeze.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .mode(ProcessFreezeMode.IMPROVEMENT_PARTIAL)
                .frozenAt(LocalDateTime.now())
                .frozenBy(TD_USER_ID)
                .liftedAt(LocalDateTime.now())
                .liftedBy(TD_USER_ID)
                .build();
        when(liftProcessFreezeUseCase.lift(eq(processId), eq("ACTIVE"), eq(TD_USER_ID), any(), any()))
                .thenReturn(lifted);

        mockMvc.perform(post("/api/v1/processes/{processId}/freeze/lift", processId)
                        .with(tdAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetMode\":\"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    private static ProcessFreeze activeFreeze(UUID processId) {
        return ProcessFreeze.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .mode(ProcessFreezeMode.AUDIT_READONLY)
                .frozenAt(LocalDateTime.now())
                .frozenBy(TD_USER_ID)
                .build();
    }

    private static RequestPostProcessor tdAuth() {
        return authentication(new UsernamePasswordAuthenticationToken(
                TD_USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_TD"))));
    }

    private static RequestPostProcessor eeAuth() {
        return authentication(new UsernamePasswordAuthenticationToken(
                EE_USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_EE"))));
    }
}
