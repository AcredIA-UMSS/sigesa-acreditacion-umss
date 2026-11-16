package com.umss.sigesa.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import com.umss.sigesa.config.AuthDataLoader;
import com.umss.sigesa.domain.exception.InvalidDateRangeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditLogGherkinIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Tag("PRD-US-022")
    @Tag("FSD-UC-017")
    @Tag("TC-12")
    @DisplayName("Rango de fechas inválido responde 422")
    void invalidDateRangeReturns422() throws Exception {
        String jdToken = login(AuthDataLoader.SEED_JD_EMAIL, AuthDataLoader.SEED_JD_PASSWORD);

        mockMvc.perform(get("/api/v1/audit/logs")
                        .header("Authorization", "Bearer " + jdToken)
                        .param("from", "2026-12-31T00:00:00")
                        .param("to", "2026-01-01T00:00:00"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("INVALID_DATE_RANGE"))
                .andExpect(jsonPath("$.message").value(InvalidDateRangeException.CLIENT_MESSAGE));

        mockMvc.perform(get("/api/v1/audit/logs/export")
                        .header("Authorization", "Bearer " + jdToken)
                        .param("from", "2026-12-31T00:00:00")
                        .param("to", "2026-01-01T00:00:00"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("INVALID_DATE_RANGE"));
    }

    @Test
    @Tag("PRD-US-022")
    @Tag("FSD-UC-017")
    @Tag("TC-12")
    @DisplayName("Aprobación de indicador por [TD] queda en bitácora")
    void indicatorApprovalIsAudited() throws Exception {
        String rawId = jdbcTemplate.queryForObject(
                "select cast(id as varchar) from indicators where code = ?",
                String.class,
                "IND-M-01");
        UUID indicatorId = UUID.fromString(rawId);

        String tdToken = login(AuthDataLoader.SEED_TD_EMAIL, AuthDataLoader.SEED_TD_PASSWORD);
        mockMvc.perform(post("/api/v1/indicators/" + indicatorId + "/approve")
                        .header("Authorization", "Bearer " + tdToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newState").value("APROBADO"));

        String jdToken = login(AuthDataLoader.SEED_JD_EMAIL, AuthDataLoader.SEED_JD_PASSWORD);
        String body = mockMvc.perform(get("/api/v1/audit/logs")
                        .header("Authorization", "Bearer " + jdToken)
                        .param("action", "INDICATOR_APPROVED")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("INDICATOR_APPROVED");
        assertThat(body).contains(indicatorId.toString());
        assertThat(body).contains(AuthDataLoader.SEED_TD_EMAIL);
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
