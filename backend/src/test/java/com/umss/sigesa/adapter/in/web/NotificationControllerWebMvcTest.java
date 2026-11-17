package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAccessDeniedHandler;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.ListMyInAppNotificationsUseCase;
import com.umss.sigesa.application.port.in.MarkAllInAppNotificationsReadUseCase;
import com.umss.sigesa.application.port.in.MarkInAppNotificationReadUseCase;
import com.umss.sigesa.domain.model.notification.InAppNotification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class NotificationControllerWebMvcTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListMyInAppNotificationsUseCase listMyInAppNotificationsUseCase;
    @MockitoBean
    private MarkInAppNotificationReadUseCase markInAppNotificationReadUseCase;
    @MockitoBean
    private MarkAllInAppNotificationsReadUseCase markAllInAppNotificationsReadUseCase;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    void listMe_returnsOwnNotifications() throws Exception {
        UUID notifId = UUID.randomUUID();
        when(listMyInAppNotificationsUseCase.list(eq(OWNER_ID), anyBoolean(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new InAppNotification(
                        notifId, null, OWNER_ID, "IndicatorApproved", "Título", "Cuerpo corto",
                        "/procesos/x", null, null, null, LocalDateTime.now()))));

        mockMvc.perform(get("/api/v1/notifications/me").with(auth(OWNER_ID, "CC")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(notifId.toString()))
                .andExpect(jsonPath("$.content[0].title").value("Título"));
    }

    @Test
    void markRead_crossUserReturns404() throws Exception {
        UUID notifId = UUID.randomUUID();
        when(markInAppNotificationReadUseCase.markRead(notifId, OWNER_ID)).thenReturn(false);

        mockMvc.perform(post("/api/v1/notifications/{id}/read", notifId).with(auth(OWNER_ID, "CC")))
                .andExpect(status().isNotFound());
    }

    @Test
    void listMe_withoutAuthReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void markAllRead_returns204() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/me/read-all").with(auth(OWNER_ID, "TD")))
                .andExpect(status().isNoContent());
    }

    private static RequestPostProcessor auth(UUID userId, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
}
