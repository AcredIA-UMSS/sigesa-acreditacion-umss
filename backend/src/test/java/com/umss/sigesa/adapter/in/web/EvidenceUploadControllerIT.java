package com.umss.sigesa.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import com.umss.sigesa.config.AuthDataLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EvidenceUploadControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Carga exitosa de evidencia versionada por un CC autenticado")
    void uploadEvidenceSuccess() throws Exception {
        String token = obtainCcToken();
        UUID indicatorId = UUID.randomUUID();
        UUID criterionId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "evidence_report.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "fake pdf content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/indicators/" + indicatorId + "/evidences")
                        .file(file)
                        .param("criterionId", criterionId.toString())
                        .param("description", "Reporte de autoevaluación 2026")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.evidenceId").exists())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.contentHash").exists())
                .andExpect(jsonPath("$.event").value("EvidenceUploaded"));
    }

    @Test
    @DisplayName("Intento de carga por parte de un Técnico [TD] es rechazado (HTTP 403)")
    void uploadEvidenceAsTdRejected() throws Exception {
        String token = obtainTdToken();
        UUID indicatorId = UUID.randomUUID();
        UUID criterionId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "evidence_report.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "fake pdf content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/indicators/" + indicatorId + "/evidences")
                        .file(file)
                        .param("criterionId", criterionId.toString())
                        .param("description", "Reporte de autoevaluación 2026")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_SCOPE"));
    }

    @Test
    @DisplayName("Carga sin clasificar (sin criterionId) es rechazada con HTTP 400")
    void uploadEvidenceWithoutCriterionRejected() throws Exception {
        String token = obtainCcToken();
        UUID indicatorId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "evidence_report.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "fake pdf content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/indicators/" + indicatorId + "/evidences")
                        .file(file)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    private String obtainCcToken() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(AuthDataLoader.SEED_CC_EMAIL, AuthDataLoader.SEED_CC_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
    }

    private String obtainTdToken() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(AuthDataLoader.SEED_TD_EMAIL, AuthDataLoader.SEED_TD_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
    }
}
