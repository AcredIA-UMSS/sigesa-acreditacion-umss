package com.umss.sigesa.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import com.umss.sigesa.config.AuthDataLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ProcessDossierExportStreamingIT — Integration Tests for Dossier Export")
class ProcessDossierExportStreamingIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Tag("FSD-UC-032")
    @DisplayName("Exportación Dossier requiere autenticación (401 en anónimo)")
    void exportRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/processes/00000000-0000-0000-0000-000000000001/dossier/export"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("FSD-UC-032")
    @DisplayName("[JD] encola exportación de dossier y recibe 202 ACCEPTED con jobId y URL de estado")
    void jdCanEnqueueDossierExport() throws Exception {
        String token = login(AuthDataLoader.SEED_JD_EMAIL, AuthDataLoader.SEED_JD_PASSWORD);

        String processesJson = mockMvc.perform(get("/api/v1/processes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<String> processIds = JsonPath.read(processesJson, "$[*].id");
        assertThat(processIds).isNotEmpty();
        String processId = processIds.getFirst();

        MvcResult exportResult = mockMvc.perform(post("/api/v1/processes/" + processId + "/dossier/export")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.statusUrl").isNotEmpty())
                .andReturn();

        String jobId = JsonPath.read(exportResult.getResponse().getContentAsString(), "$.jobId");
        assertThat(jobId).isNotNull();

        mockMvc.perform(get("/api/v1/processes/" + processId + "/dossier/export-jobs/" + jobId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(jobId))
                .andExpect(jsonPath("$.status").isNotEmpty());
    }

    private String login(String email, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
    }
}
