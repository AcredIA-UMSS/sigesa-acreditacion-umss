package com.umss.sigesa.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import com.umss.sigesa.config.AuthDataLoader;
import com.umss.sigesa.config.DevSeedData;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CoverageMatrixGherkinIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SpringDataAccreditationProcessRepository processRepository;

    @Test
    @Tag("FSD-UC-039")
    @Tag("TC-COVERAGE")
    @DisplayName("El par ve criterio, indicador, evidencias y estado")
    void peerSeesCriterionIndicatorEvidenceAndStatus() throws Exception {
        String token = login(AuthDataLoader.SEED_JD_EMAIL, AuthDataLoader.SEED_JD_PASSWORD);
        UUID processId = processWithTree();

        MvcResult result = mockMvc.perform(get("/api/v1/processes/{processId}/coverage-matrix", processId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processId").value(processId.toString()))
                .andExpect(jsonPath("$.totals.indicators").isNumber())
                .andExpect(jsonPath("$.dimensions[0].components[0].criteria[0].indicators[0].code").isString())
                .andExpect(jsonPath("$.dimensions[0].components[0].criteria[0].indicators[0].status").isString())
                .andExpect(jsonPath("$.dimensions[0].components[0].criteria[0].indicators[0].evidenceCount").isNumber())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        List<String> codes = JsonPath.read(body, "$..indicators[*].code");
        assertThat(codes).isNotEmpty();
        String criterion = JsonPath.read(body, "$.dimensions[0].components[0].criteria[0].name");
        String dimension = JsonPath.read(body, "$.dimensions[0].name");
        assertThat(criterion).isNotBlank();
        assertThat(dimension).isNotBlank();
    }

    @Test
    @Tag("FSD-UC-039")
    @Tag("TC-COVERAGE")
    @DisplayName("PDF imprimible distinto del dossier")
    void printablePdfIsNotZip() throws Exception {
        String token = login(AuthDataLoader.SEED_TD_EMAIL, AuthDataLoader.SEED_TD_PASSWORD);
        UUID processId = processWithTree();

        MvcResult result = mockMvc.perform(get("/api/v1/processes/{processId}/coverage-matrix/pdf", processId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(result.getResponse().getContentType()).contains("application/pdf");
        assertThat(body).startsWith("%PDF".getBytes());
        assertThat(body.length < 2 || body[0] != 'P' || body[1] != 'K').isTrue();
    }

    @Test
    @Tag("FSD-UC-039")
    @Tag("TC-COVERAGE")
    @DisplayName("Coordinador no ve otra carrera")
    void coordinatorDoesNotSeeOtherCareer() throws Exception {
        String token = login(AuthDataLoader.SEED_CC2_EMAIL, AuthDataLoader.SEED_CC2_PASSWORD);
        mockMvc.perform(get("/api/v1/processes/{processId}/coverage-matrix", processWithTree())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PROCESS_NOT_FOUND"));
    }

    private UUID processWithTree() {
        return processRepository.findByCareerIdInOrderByStartDateDesc(List.of(DevSeedData.PROGRAM_ING_MECANICA))
                .stream()
                .map(AccreditationProcessJpaEntity::getId)
                .findFirst()
                .orElse(DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE);
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
