package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.ProcessExceptionHandler;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.in.ArchiveTemplateUseCase;
import com.umss.sigesa.application.port.in.CreateTemplateUseCase;
import com.umss.sigesa.application.port.in.DeleteTemplateUseCase;
import com.umss.sigesa.application.port.in.DuplicateTemplateUseCase;
import com.umss.sigesa.application.port.in.GetTemplateUseCase;
import com.umss.sigesa.application.port.in.ListTemplatesUseCase;
import com.umss.sigesa.application.port.in.PublishTemplateUseCase;
import com.umss.sigesa.application.port.in.UpdateTemplateUseCase;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplatePhase;
import com.umss.sigesa.domain.model.TemplateStatus;
import com.umss.sigesa.domain.model.TemplateSubphase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TemplateController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class, ProcessExceptionHandler.class})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class TemplateControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateTemplateUseCase createTemplateUseCase;
    @MockitoBean
    private UpdateTemplateUseCase updateTemplateUseCase;
    @MockitoBean
    private GetTemplateUseCase getTemplateUseCase;
    @MockitoBean
    private ListTemplatesUseCase listTemplatesUseCase;
    @MockitoBean
    private PublishTemplateUseCase publishTemplateUseCase;
    @MockitoBean
    private ArchiveTemplateUseCase archiveTemplateUseCase;
    @MockitoBean
    private DuplicateTemplateUseCase duplicateTemplateUseCase;
    @MockitoBean
    private DeleteTemplateUseCase deleteTemplateUseCase;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    @WithMockUser(roles = "JD")
    void shouldListTemplatesForJd() throws Exception {
        UUID templateId = UUID.randomUUID();
        when(listTemplatesUseCase.list(any(), any())).thenReturn(List.of(
                Template.builder()
                        .id(templateId)
                        .name("CEUB 2026")
                        .type("CEUB")
                        .status(TemplateStatus.PUBLISHED)
                        .phases(List.of(TemplatePhase.builder()
                                .name("Fase")
                                .order(1)
                                .subphases(List.of(TemplateSubphase.builder()
                                        .name("Sub")
                                        .order(1)
                                        .referenceUrl("https://duea.umss.edu.bo/ref")
                                        .build()))
                                .build()))
                        .build()
        ));

        mockMvc.perform(get("/api/v1/templates")
                        .with(user("testjd").roles("JD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("CEUB 2026"))
                .andExpect(jsonPath("$[0].phaseCount").value(1))
                .andExpect(jsonPath("$[0].subphaseCount").value(1));
    }

    @Test
    @WithMockUser(roles = "CC")
    void shouldRejectCcAccessToTemplates() throws Exception {
        mockMvc.perform(get("/api/v1/templates")
                        .with(user("testcc").roles("CC")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "JD")
    void shouldCreateTemplate() throws Exception {
        UUID templateId = UUID.randomUUID();
        when(createTemplateUseCase.create(any(Template.class))).thenReturn(
                Template.builder()
                        .id(templateId)
                        .name("CEUB Piloto")
                        .type("CEUB")
                        .status(TemplateStatus.DRAFT)
                        .phases(List.of(TemplatePhase.builder()
                                .name("Autoevaluación")
                                .order(1)
                                .subphases(List.of(TemplateSubphase.builder()
                                        .name("Diagnóstico")
                                        .order(1)
                                        .referenceUrl("https://duea.umss.edu.bo/guia/diagnostico")
                                        .build()))
                                .build()))
                        .build()
        );

        String body = """
                {
                  "name": "CEUB Piloto",
                  "type": "CEUB",
                  "phases": [
                    {
                      "name": "Autoevaluación",
                      "order": 1,
                      "subphases": [
                        {
                          "name": "Diagnóstico",
                          "order": 1,
                          "referenceUrl": "https://duea.umss.edu.bo/guia/diagnostico"
                        }
                      ]
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/templates")
                        .with(user("testjd").roles("JD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.subphaseCount").value(1));
    }
}
