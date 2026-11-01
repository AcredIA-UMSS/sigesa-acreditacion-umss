package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.AuditExceptionHandler;
import com.umss.sigesa.adapter.in.web.advice.AuthExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.GetAuditLogsUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.domain.model.AuditAction;
import com.umss.sigesa.domain.model.AuditEvent;
import com.umss.sigesa.domain.model.AuditLogQuery;
import com.umss.sigesa.domain.model.AuditResourceType;
import com.umss.sigesa.domain.model.AuditResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuditLogController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        AuthExceptionHandler.class,
        AuditExceptionHandler.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class AuditLogControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAuditLogsUseCase getAuditLogsUseCase;

    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @MockitoBean
    private AuditLogPort auditLogPort;

    @Test
    void jdListsFilteredPageInLocalTime() throws Exception {
        UUID actorId = UUID.randomUUID();
        when(getAuditLogsUseCase.getAuditLogs(any(AuditLogQuery.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new AuditEvent(
                        UUID.randomUUID(),
                        actorId,
                        "jd@umss.edu.bo",
                        AuditAction.LOGIN,
                        AuditResourceType.USER,
                        actorId,
                        "Inicio de sesión exitoso",
                        Instant.parse("2026-10-29T15:00:00Z"),
                        AuditResult.SUCCESS,
                        "127.0.0.1",
                        "{\"ignored\":true}"
                ))));

        mockMvc.perform(get("/api/v1/audit/logs")
                        .param("action", "LOGIN")
                        .param("result", "SUCCESS")
                        .param("size", "20")
                        .with(user("jd").roles("JD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("LOGIN"))
                .andExpect(jsonPath("$.content[0].actorEmail").value("jd@umss.edu.bo"))
                .andExpect(jsonPath("$.content[0].occurredAt").value("2026-10-29T11:00:00-04:00"))
                .andExpect(jsonPath("$.content[0].payload").value(nullValue()));
    }

    @Test
    void ccCannotReadTheLog() throws Exception {
        mockMvc.perform(get("/api/v1/audit/logs").with(user("cc").roles("CC")))
                .andExpect(status().isForbidden());
        verify(getAuditLogsUseCase, never()).getAuditLogs(any(), any());
    }

    @Test
    void anonymousCannotReadTheLog() throws Exception {
        mockMvc.perform(get("/api/v1/audit/logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invertedRangeIsUnprocessable() throws Exception {
        when(getAuditLogsUseCase.getAuditLogs(any(), any()))
                .thenThrow(new com.umss.sigesa.domain.exception.InvalidFilterException(
                        "El rango de fechas es inválido."));

        mockMvc.perform(get("/api/v1/audit/logs")
                        .param("from", "2026-02-01T00:00:00Z")
                        .param("to", "2026-01-01T00:00:00Z")
                        .with(user("jd").roles("JD")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("INVALID_FILTER"));
    }

    @Test
    void unknownActionIsUnprocessable() throws Exception {
        mockMvc.perform(get("/api/v1/audit/logs")
                        .param("action", "DROP")
                        .with(user("jd").roles("JD")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("INVALID_FILTER"));
        verify(getAuditLogsUseCase, never()).getAuditLogs(any(), any());
    }

    @Test
    void detailIncludesPayload() throws Exception {
        UUID id = UUID.randomUUID();
        when(getAuditLogsUseCase.getAuditLog(id)).thenReturn(Optional.of(new AuditEvent(
                id, null, null, AuditAction.EVIDENCE_UPLOADED, AuditResourceType.EVIDENCE,
                UUID.randomUUID(), "Carga de evidencia", Instant.parse("2026-10-29T15:00:00Z"),
                AuditResult.SUCCESS, null, "{\"indicatorId\":\"abc\"}")));

        mockMvc.perform(get("/api/v1/audit/logs/" + id).with(user("jd").roles("JD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload").value("{\"indicatorId\":\"abc\"}"));
    }

    @Test
    void repeatedActionsStayOnTheQuery() throws Exception {
        when(getAuditLogsUseCase.getAuditLogs(any(AuditLogQuery.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/audit/logs")
                        .param("action", "LOGIN,LOGOUT")
                        .with(user("jd").roles("JD")))
                .andExpect(status().isOk());

        var captor = org.mockito.ArgumentCaptor.forClass(AuditLogQuery.class);
        verify(getAuditLogsUseCase).getAuditLogs(captor.capture(), any());
        org.junit.jupiter.api.Assertions.assertEquals(
                java.util.Set.of(AuditAction.LOGIN, AuditAction.LOGOUT),
                captor.getValue().actions());
    }

    @Test
    void exportReturnsCsvWithoutPayload() throws Exception {
        when(getAuditLogsUseCase.exportAuditLogs(any(AuditLogQuery.class)))
                .thenReturn(List.of(new AuditEvent(
                        UUID.randomUUID(), null, "jd@umss.edu.bo", AuditAction.LOGOUT,
                        AuditResourceType.USER, null, "Cierre de sesión",
                        Instant.parse("2026-10-29T15:00:00Z"), AuditResult.SUCCESS, "127.0.0.1",
                        "{\"role\":\"JD\"}")));

        mockMvc.perform(get("/api/v1/audit/logs/export").with(user("jd").roles("JD")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cierre de sesión")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("role"))));
    }

    @Test
    void ordinaryApiCannotDeleteEvents() throws Exception {
        mockMvc.perform(delete("/api/v1/audit/logs").with(user("jd").roles("JD")))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(delete("/api/v1/audit/logs/" + UUID.randomUUID()).with(user("jd").roles("JD")))
                .andExpect(status().isMethodNotAllowed());
    }
}
