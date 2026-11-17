package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAccessDeniedHandler;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.EvaluationExceptionHandler;
import com.umss.sigesa.adapter.in.web.advice.ProcessExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.CreateImprovementPlanItemUseCase;
import com.umss.sigesa.application.port.in.ImportImprovementPlanFromFodaUseCase;
import com.umss.sigesa.application.port.in.ListImprovementPlanUseCase;
import com.umss.sigesa.application.port.in.UpdateImprovementPlanItemUseCase;
import com.umss.sigesa.domain.exception.ValidationException;
import com.umss.sigesa.domain.model.ImportImprovementPlanResult;
import com.umss.sigesa.domain.model.ImprovementPlanItem;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ImprovementPlanController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        EvaluationExceptionHandler.class,
        ProcessExceptionHandler.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class ImprovementPlanControllerWebMvcTest {

    private static final UUID CC_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TD_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListImprovementPlanUseCase listImprovementPlanUseCase;
    @MockitoBean
    private CreateImprovementPlanItemUseCase createImprovementPlanItemUseCase;
    @MockitoBean
    private UpdateImprovementPlanItemUseCase updateImprovementPlanItemUseCase;
    @MockitoBean
    private ImportImprovementPlanFromFodaUseCase importImprovementPlanFromFodaUseCase;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    void list_returns200() throws Exception {
        UUID processId = UUID.randomUUID();
        when(listImprovementPlanUseCase.list(eq(processId), eq(TD_USER_ID), any(), any()))
                .thenReturn(List.of(item(processId, "Objetivo")));

        mockMvc.perform(get("/api/v1/processes/{processId}/improvement-plan", processId).with(tdAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].objective").value("Objetivo"));
    }

    @Test
    void create_returns201() throws Exception {
        UUID processId = UUID.randomUUID();
        when(createImprovementPlanItemUseCase.create(any())).thenReturn(item(processId, "Nuevo"));

        mockMvc.perform(post("/api/v1/processes/{processId}/improvement-plan", processId)
                        .with(ccAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"objective\":\"Nuevo\",\"progress\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.objective").value("Nuevo"));
    }

    @Test
    void create_tdForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/processes/{processId}/improvement-plan", UUID.randomUUID())
                        .with(tdAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"objective\":\"Nuevo\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_validationError() throws Exception {
        UUID processId = UUID.randomUUID();
        when(createImprovementPlanItemUseCase.create(any()))
                .thenThrow(new ValidationException("El objetivo no puede estar vacío."));

        mockMvc.perform(post("/api/v1/processes/{processId}/improvement-plan", processId)
                        .with(ccAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"objective\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void importFromFoda_returns200() throws Exception {
        UUID processId = UUID.randomUUID();
        when(importImprovementPlanFromFodaUseCase.importFromFoda(eq(processId), eq(CC_USER_ID), any(), any()))
                .thenReturn(new ImportImprovementPlanResult(2, 0, List.of(item(processId, "Debilidad"))));

        mockMvc.perform(post("/api/v1/processes/{processId}/improvement-plan/import-from-foda", processId)
                        .with(ccAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount").value(2))
                .andExpect(jsonPath("$.skippedCount").value(0));
    }

    @Test
    void update_returns200() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(updateImprovementPlanItemUseCase.update(any())).thenReturn(item(processId, "Editado"));

        mockMvc.perform(put("/api/v1/processes/{processId}/improvement-plan/{itemId}", processId, itemId)
                        .with(ccAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"objective\":\"Editado\",\"progress\":40}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objective").value("Editado"));
    }

    private static ImprovementPlanItem item(UUID processId, String objective) {
        return ImprovementPlanItem.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .objective(objective)
                .progress(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static RequestPostProcessor ccAuth() {
        return authentication(new UsernamePasswordAuthenticationToken(
                CC_USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_CC"))));
    }

    private static RequestPostProcessor tdAuth() {
        return authentication(new UsernamePasswordAuthenticationToken(
                TD_USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_TD"))));
    }
}
