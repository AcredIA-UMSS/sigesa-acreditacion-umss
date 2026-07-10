package com.umss.sigesa.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import com.umss.sigesa.config.AuthDataLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticatedApiSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("US-003: /api/v1/dashboards/me/summary sin token → 401")
    void dashboardSummaryWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/me/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("FSD-UC-001 A1: login con email no @umss.edu.bo → 401 genérico")
    void loginInvalidEmailDomainReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@gmail.com","password":"secret"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTH_INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    @DisplayName("FSD-UC-001 A1: login email vacío → 401 genérico (stack real)")
    void loginBlankEmailReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"","password":"secret"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("FSD-UC-001 A1: login password vacío → 401 genérico (stack real)")
    void loginBlankPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"jd@umss.edu.bo","password":""}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("US-003: /api/v1/processes sin token → 401")
    void processesWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Perímetro JWT: POST /api/v1/processes con token — no 401 (auth OK)")
    void processesWithValidJwtPassesSecurity() throws Exception {
        String token = obtainSeedJdToken();

        mockMvc.perform(post("/api/v1/processes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Perímetro JWT: /api/v1/dashboards/me/summary con token válido → 200")
    void dashboardSummaryWithValidJwtReturns200() throws Exception {
        String token = obtainSeedJdToken();

        mockMvc.perform(get("/api/v1/dashboards/me/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("MOD-PROCESS: JD list templates → 200")
    void listTemplatesAsJdReturns200() throws Exception {
        String token = obtainSeedJdToken();

        mockMvc.perform(get("/api/v1/templates")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("MOD-EVIDENCE: CC list indicators → 200")
    void listIndicatorsAsCcReturns200() throws Exception {
        String token = obtainSeedToken(AuthDataLoader.SEED_CC_EMAIL, AuthDataLoader.SEED_CC_PASSWORD);

        mockMvc.perform(get("/api/v1/indicators")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("MOD-EVIDENCE: CC search evidences → 200")
    void searchEvidencesAsCcReturns200() throws Exception {
        String token = obtainSeedToken(AuthDataLoader.SEED_CC_EMAIL, AuthDataLoader.SEED_CC_PASSWORD);

        mockMvc.perform(get("/api/v1/evidences/search")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String obtainSeedJdToken() throws Exception {
        return obtainSeedToken(AuthDataLoader.SEED_JD_EMAIL, AuthDataLoader.SEED_JD_PASSWORD);
    }

    private String obtainSeedToken(String email, String password) throws Exception {
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
