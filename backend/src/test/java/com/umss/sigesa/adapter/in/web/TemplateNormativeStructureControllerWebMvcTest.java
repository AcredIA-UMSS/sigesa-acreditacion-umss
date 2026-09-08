package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.ProcessExceptionHandler;
import com.umss.sigesa.adapter.in.web.mapper.NormativeStructureWebMapper;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.GetTemplateUseCase;
import com.umss.sigesa.application.port.in.TemplateNormativeStructureUseCases;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.domain.exception.TemplateIndicatorIncompleteException;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TemplateNormativeStructureController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        ProcessExceptionHandler.class,
        NormativeStructureWebMapper.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class TemplateNormativeStructureControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetTemplateUseCase getTemplateUseCase;
    @MockitoBean
    private TemplateNormativeStructureUseCases templateNormativeStructureUseCases;
    @MockitoBean
    private NormativeHierarchyQueryPort normativeHierarchyQueryPort;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    @WithMockUser(roles = "JD")
    void shouldCreateLevel1ForJd() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();

        when(getTemplateUseCase.getById(templateId)).thenReturn(draftTemplate(templateId, "CEUB"));
        when(templateNormativeStructureUseCases.addLevel1(eq(templateId), eq("Dimensión 1"), eq(1), eq("Desc")))
                .thenReturn(TemplateLevel1Node.builder()
                        .id(level1Id)
                        .name("Dimensión 1")
                        .order(1)
                        .description("Desc")
                        .build());

        mockMvc.perform(post("/api/v1/templates/{templateId}/level1-nodes", templateId)
                        .with(user("testjd").roles("JD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Dimensión 1","order":1,"description":"Desc"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dimensión 1"))
                .andExpect(jsonPath("$.label").value("Área"))
                .andExpect(jsonPath("$.order").value(1));
    }

    @Test
    @WithMockUser(roles = "TD")
    void shouldRejectNonJdMutations() throws Exception {
        UUID templateId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/templates/{templateId}/level1-nodes", templateId)
                        .with(user("testtd").roles("TD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Dimensión 1","order":1}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "JD")
    void shouldReturn400WhenIndicatorIncomplete() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID level3Id = UUID.randomUUID();

        when(templateNormativeStructureUseCases.addIndicator(
                eq(level3Id), eq("IND-01"), eq("Desc"), any(), eq(1), eq("http://invalid")))
                .thenThrow(new TemplateIndicatorIncompleteException(
                        "El indicador requiere referenceUrl HTTPS válido."));

        mockMvc.perform(post("/api/v1/templates/{templateId}/level3-nodes/{level3Id}/indicators",
                        templateId, level3Id)
                        .with(user("testjd").roles("JD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"IND-01","description":"Desc","weight":1,"order":1,"referenceUrl":"http://invalid"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("TEMPLATE_INDICATOR_INCOMPLETE"));
    }

    private Template draftTemplate(UUID templateId, String type) {
        return Template.builder()
                .id(templateId)
                .name("Plantilla test")
                .type(type)
                .status(TemplateStatus.DRAFT)
                .phases(List.of())
                .build();
    }
}
