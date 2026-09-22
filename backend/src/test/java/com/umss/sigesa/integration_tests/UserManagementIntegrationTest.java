package com.umss.sigesa.integration_tests;

import com.jayway.jsonpath.JsonPath;
import com.umss.sigesa.adapter.out.persistance.AppUserJpaRepository;
import com.umss.sigesa.adapter.out.persistance.UserProgramAssignmentJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AppUserEntity;
import com.umss.sigesa.config.AuthDataLoader;
import com.umss.sigesa.config.DevSeedData;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;
import org.junit.jupiter.api.AfterEach;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integration Tests: User Management Lifecycle (Frontend <-> Backend <-> DB)")
class UserManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserJpaRepository userJpaRepository;

    @Autowired
    private UserProgramAssignmentJpaRepository assignmentJpaRepository;

    private String jdAuthToken;
    private final List<UUID> createdUserIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        // Arrange: Authenticate as JD (Jefe de Departamento) to obtain Bearer token for admin operations
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(AuthDataLoader.SEED_JD_EMAIL, AuthDataLoader.SEED_JD_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        jdAuthToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");
    }

    @AfterEach
    void tearDown() {
        // Teardown: Clean up entities created during test execution to maintain database isolation
        for (UUID userId : createdUserIds) {
            try {
                assignmentJpaRepository.findAll().stream()
                        .filter(a -> userId.equals(a.getUserId()))
                        .forEach(assignmentJpaRepository::delete);
                userJpaRepository.deleteById(userId);
            } catch (Exception ignored) {
                // Ignore cleanup errors for idempotency
            }
        }
        createdUserIds.clear();
    }

    @Test
    @DisplayName("AAA - Register user as JD: HTTP 201 -> Verify DB Persistence -> Activate & Authenticate")
    void registerUser_persistsInDatabaseAndAllowsLogin() throws Exception {
        // ARRANGE
        String newEmail = "test.integration.cc@umss.edu.bo";
        String password = "IntegrationPassword2026!";

        String requestBody = """
                {
                  "email": "%s",
                  "role": "CC",
                  "programId": "%s",
                  "firstName": "Carlos",
                  "lastName": "Coordinador",
                  "phoneNumber": "78901234",
                  "password": "%s"
                }
                """.formatted(newEmail, DevSeedData.PROGRAM_INF_SIS, password);

        // ACT (REST Endpoint execution)
        MvcResult result = mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        String userIdStr = JsonPath.read(responseJson, "$.userId");
        UUID createdUserId = UUID.fromString(userIdStr);
        createdUserIds.add(createdUserId);

        // ASSERT DB STATE (Direct Database Persistence Verification)
        Optional<AppUserEntity> persistedUserOpt = userJpaRepository.findById(createdUserId);
        assertThat(persistedUserOpt).isPresent();

        AppUserEntity persistedUser = persistedUserOpt.get();
        assertThat(persistedUser.getEmail()).isEqualTo(newEmail);
        assertThat(persistedUser.getRole()).isEqualTo(Role.CC);
        assertThat(persistedUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(persistedUser.getFirstName()).isEqualTo("Carlos");
        assertThat(persistedUser.getLastName()).isEqualTo("Coordinador");
        assertThat(persistedUser.getPhoneNumber()).isEqualTo("78901234");
        assertThat(persistedUser.getPasswordHash()).isNotEqualTo(password); // Password hashed

        // ACT: Update status in DB to ACTIVE to verify login capability
        persistedUser.setStatus(UserStatus.ACTIVE);
        userJpaRepository.save(persistedUser);

        // ACT & ASSERT: Authenticate as newly registered user via REST API
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(newEmail, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.role").value("CC"));
    }

    @Test
    @DisplayName("AAA - Deactivate user as JD: HTTP 204 -> Verify DB Status DEACTIVATED -> Login Fails (401)")
    void deactivateUser_updatesDatabaseStatusAndBlocksAuthentication() throws Exception {
        // ARRANGE: Create user to deactivate
        String email = "deactivate.test@umss.edu.bo";
        String password = "TestPassword123!";

        String createBody = """
                {
                  "email": "%s",
                  "role": "CC",
                  "programId": "%s",
                  "firstName": "Laura",
                  "lastName": "Perez",
                  "phoneNumber": "71122334",
                  "password": "%s"
                }
                """.formatted(email, DevSeedData.PROGRAM_INF_SIS, password);

        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        UUID targetUserId = UUID.fromString(JsonPath.read(createResult.getResponse().getContentAsString(), "$.userId"));
        createdUserIds.add(targetUserId);

        // Activate in DB first so we can verify deactivation transition
        AppUserEntity initialUser = userJpaRepository.findById(targetUserId).orElseThrow();
        initialUser.setStatus(UserStatus.ACTIVE);
        userJpaRepository.save(initialUser);

        // ACT: Deactivate via REST API
        mockMvc.perform(patch("/api/v1/admin/users/{id}/deactivate", targetUserId)
                        .header("Authorization", "Bearer " + jdAuthToken))
                .andExpect(status().isNoContent());

        // ASSERT DB STATE: Check user status changed to DEACTIVATED in database
        AppUserEntity deactivatedUser = userJpaRepository.findById(targetUserId).orElseThrow();
        assertThat(deactivatedUser.getStatus()).isEqualTo(UserStatus.DEACTIVATED);

        // ACT & ASSERT: Attempting login with deactivated user must be rejected with 401 Unauthorized
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("AAA - List users as JD: HTTP 200 -> Returns database user list with accurate payload")
    void listUsers_returnsPersistedUsersFromDatabase() throws Exception {
        // ARRANGE & ACT
        MvcResult result = mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .param("role", "JD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        // ASSERT: Check returned list corresponds to database records
        List<String> emails = JsonPath.read(result.getResponse().getContentAsString(), "$[*].email");
        assertThat(emails).contains(AuthDataLoader.SEED_JD_EMAIL);
    }
}
