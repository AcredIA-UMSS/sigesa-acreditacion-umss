package com.umss.sigesa.integration_tests;

import com.jayway.jsonpath.JsonPath;
import com.umss.sigesa.config.AuthDataLoader;
import com.umss.sigesa.config.DevSeedData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integration Tests: Security Perimeter, RBAC, SQL & Prompt Injection Defenses")
class SecurityAndNegativeCasesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String jdAuthToken;
    private String ccAuthToken;

    @BeforeEach
    void setUp() throws Exception {
        // Authenticate JD
        MvcResult jdLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(AuthDataLoader.SEED_JD_EMAIL, AuthDataLoader.SEED_JD_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        jdAuthToken = JsonPath.read(jdLoginResult.getResponse().getContentAsString(), "$.accessToken");

        // Authenticate CC
        MvcResult ccLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(AuthDataLoader.SEED_CC_EMAIL, AuthDataLoader.SEED_CC_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        ccAuthToken = JsonPath.read(ccLoginResult.getResponse().getContentAsString(), "$.accessToken");
    }

    @Test
    @DisplayName("AAA - Unauthenticated Requests: HTTP 401 Unauthorized for Protected Endpoints")
    void unauthenticatedRequests_return401Unauthorized() throws Exception {
        // ACT & ASSERT: Accessing protected endpoints without Bearer token
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        mockMvc.perform(post("/api/v1/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        mockMvc.perform(delete("/api/v1/processes/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        mockMvc.perform(get("/api/v1/assistant/status"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("AAA - Insufficient Role (RBAC): HTTP 403 Forbidden when CC attempts JD Operations")
    void insufficientRoleRequests_return403Forbidden() throws Exception {
        // ACT & ASSERT: CC attempting user administration (JD only)
        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + ccAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "unauthorized.cc@umss.edu.bo",
                                  "role": "CC",
                                  "programId": "%s",
                                  "firstName": "Test",
                                  "lastName": "User",
                                  "phoneNumber": "71234567",
                                  "password": "Password123!"
                                }
                                """.formatted(DevSeedData.PROGRAM_INF_SIS)))
                .andExpect(status().isForbidden());

        // ACT & ASSERT: CC attempting process deletion (JD only)
        mockMvc.perform(delete("/api/v1/processes/{id}", DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE)
                        .header("Authorization", "Bearer " + ccAuthToken))
                .andExpect(status().isForbidden());

        // ACT & ASSERT: CC attempting to access 'users' copilot agent (JD only)
        mockMvc.perform(get("/api/v1/assistant/status")
                        .param("agent", "users")
                        .header("Authorization", "Bearer " + ccAuthToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("AAA - SQL Injection Defense in Search API: Safely Handled via Parameterized JPA Queries")
    void sqlInjectionInSearchQuery_isHandledSafelyWithoutDbError() throws Exception {
        // ACT & ASSERT: Inject SQL snippet into program search parameter
        MvcResult result = mockMvc.perform(get("/api/v1/programs")
                        .param("q", "' OR '1'='1'; DROP TABLE app_users; --")
                        .header("Authorization", "Bearer " + jdAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        // Parameterized query handles injection safely; no exception or table destruction occurs
        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).isNotNull();
    }

    @Test
    @DisplayName("AAA - Prompt & SQL Injection Defense in AI Assistant: HTTP 400 Bad Request")
    void promptAndSqlInjectionInAssistant_rejectedWith400BadRequest() throws Exception {
        // Injection payload 1: UNION SELECT
        mockMvc.perform(post("/api/v1/assistant/chat")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "UNION SELECT id, email, password_hash FROM app_users;"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ASSISTANT_INVALID_INPUT"));

        // Injection payload 2: Script XSS
        mockMvc.perform(post("/api/v1/assistant/chat")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "<script>alert('XSS Vulnerability')</script>"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ASSISTANT_INVALID_INPUT"));

        // Injection payload 3: SQL Drop Table
        mockMvc.perform(post("/api/v1/assistant/chat")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "'; DROP TABLE accreditation_processes; --"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ASSISTANT_INVALID_INPUT"));
    }

    @Test
    @DisplayName("AAA - Out-of-Scope Prompt Handling in AI Assistant: Safe Refusal / Resolution Path")
    void outOfScopePromptInAssistant_returnsSafeResponse() throws Exception {
        // ACT & ASSERT: Prompt asking for university budget
        mockMvc.perform(post("/api/v1/assistant/chat")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "¿Cuál es el presupuesto de la universidad para el año 2027?"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").exists())
                .andExpect(jsonPath("$.path").value("OUT_OF_SCOPE"));
    }

    @Test
    @DisplayName("AAA - DTO Validation Errors: HTTP 400 Bad Request for Malformed Input Payload")
    void dtoValidationErrors_return400BadRequest() throws Exception {
        // Invalid phone number (less than 8 digits)
        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid.phone@umss.edu.bo",
                                  "role": "CC",
                                  "programId": "%s",
                                  "firstName": "Invalid",
                                  "lastName": "Phone",
                                  "phoneNumber": "1234",
                                  "password": "Password123!"
                                }
                                """.formatted(DevSeedData.PROGRAM_INF_SIS)))
                .andExpect(status().isBadRequest());
    }
}
