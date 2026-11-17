package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAccessDeniedHandler;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.NotificationModuleExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.NotificationModulePort;
import com.umss.sigesa.domain.exception.NotificationModuleUnavailableException;
import com.umss.sigesa.domain.model.NotificationModuleSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationE3Controller.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        NotificationModuleExceptionHandler.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class NotificationE3ControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationModulePort notificationModulePort;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    void inspect_isPublicAndReturnsSnapshot() throws Exception {
        when(notificationModulePort.inspect(any(Duration.class)))
                .thenReturn(new NotificationModuleSnapshot(3, 8, "in_process"));

        mockMvc.perform(get("/api/v1/e3/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingCount").value(3))
                .andExpect(jsonPath("$.origin").value("in_process"));
    }

    @Test
    void inspect_mapsUnavailableTo503() throws Exception {
        when(notificationModulePort.inspect(any(Duration.class)))
                .thenThrow(new NotificationModuleUnavailableException("down"));

        mockMvc.perform(get("/api/v1/e3/notifications"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("NOTIFICATION_MODULE_UNAVAILABLE"));
    }
}
