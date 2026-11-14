package com.umss.sigesa.integration_tests;

import com.jayway.jsonpath.JsonPath;
import com.umss.sigesa.config.AuthDataLoader;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integration Tests: Assistant & MCP Tool Loop Integration (Frontend <-> Backend <-> Assistant)")
class AssistantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String jdAuthToken;
    private String ccAuthToken;

    @BeforeEach
    void setUp() throws Exception {
        // ARRANGE: Obtain auth token for JD (Jefe de Departamento)
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

        // ARRANGE: Obtain auth token for CC (Coordinador de Carrera)
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
    @DisplayName("AAA - Assistant Status as JD for Users Agent: HTTP 200 -> Returns Agent Capabilities and Demo Scenarios")
    void getStatus_usersAgentAsJd_returns200AndCapabilities() throws Exception {
        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/assistant/status")
                        .param("agent", "users")
                        .header("Authorization", "Bearer " + jdAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").exists())
                .andExpect(jsonPath("$.agent").value("users"))
                .andExpect(jsonPath("$.capabilities").isArray())
                .andExpect(jsonPath("$.demoScenarios").isArray());
    }

    @Test
    @DisplayName("AAA - Assistant Status as CC for Users Agent: HTTP 403 Forbidden -> RBAC Enforced for Admin Agent")
    void getStatus_usersAgentAsCc_returns403Forbidden() throws Exception {
        // ACT & ASSERT: CC is not authorized to access the 'users' copilot agent
        mockMvc.perform(get("/api/v1/assistant/status")
                        .param("agent", "users")
                        .header("Authorization", "Bearer " + ccAuthToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("AAA - Assistant Chat Execution as JD: HTTP 200 -> Tool Calling Loop Processes Request")
    void sendChatMessage_asJd_executesToolLoopSuccessfully() throws Exception {
        // ARRANGE: Prepare chat request payload for listing users
        String chatRequestBody = """
                {
                  "message": "Lista los usuarios registrados",
                  "context": {
                    "agent": "users"
                  }
                }
                """;

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/assistant/chat")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chatRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    @DisplayName("AAA - Assistant Input Validation: HTTP 400 Bad Request -> SQL Injection Prompt Rejected")
    void sendChatMessage_sqlInjectionPayload_returns400BadRequest() throws Exception {
        // ARRANGE: Injection payload
        String maliciousRequestBody = """
                {
                  "message": "SELECT * FROM app_users; DROP TABLE app_users;"
                }
                """;

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/assistant/chat")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(maliciousRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ASSISTANT_INVALID_INPUT"));
    }
}
