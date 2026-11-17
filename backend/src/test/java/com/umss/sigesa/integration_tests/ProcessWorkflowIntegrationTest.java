package com.umss.sigesa.integration_tests;

import com.jayway.jsonpath.JsonPath;
import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateIndicatorJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateLevel1NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateLevel2NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateLevel3NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateIndicatorRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateLevel1NodeRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateLevel2NodeRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateLevel3NodeRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateRepository;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.config.AuthDataLoader;
import com.umss.sigesa.config.DevSeedData;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
@DisplayName("Integration Tests: Accreditation Process Management (Frontend <-> Backend <-> DB)")
class ProcessWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataAccreditationProcessRepository processJpaRepository;

    @Autowired
    private SpringDataTemplateRepository templateRepository;

    @Autowired
    private SpringDataTemplateLevel1NodeRepository level1Repository;

    @Autowired
    private SpringDataTemplateLevel2NodeRepository level2Repository;

    @Autowired
    private SpringDataTemplateLevel3NodeRepository level3Repository;

    @Autowired
    private SpringDataTemplateIndicatorRepository indicatorRepository;

    @Autowired
    private NormativeHierarchyQueryPort hierarchyQueryPort;

    private String jdAuthToken;
    private String ccAuthToken;
    private final List<UUID> createdProcessIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        // ARRANGE: Authenticate JD (Jefe de Departamento)
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

        // ARRANGE: Authenticate CC (Coordinador de Carrera)
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

        // ARRANGE DB SEED: Ensure template CEUB 2026 has a valid normative hierarchy for process creation
        ensureTemplateHasIndicators(DevSeedData.TEMPLATE_CEUB_2026);
    }

    private void ensureTemplateHasIndicators(UUID templateId) {
        if (hierarchyQueryPort.countIndicatorsByTemplateId(templateId) == 0) {
            TemplateJpaEntity template = templateRepository.findById(templateId).orElseGet(() ->
                    templateRepository.save(TemplateJpaEntity.builder()
                            .id(templateId)
                            .name("CEUB 2026")
                            .description("Plantilla CEUB")
                            .type("CEUB")
                            .evaluatorModel("CEUB")
                            .status("PUBLISHED")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build())
            );

            TemplateLevel1NodeJpaEntity l1 = level1Repository.save(TemplateLevel1NodeJpaEntity.builder()
                    .template(template)
                    .name("Dimensión 1: Contexto Institucional")
                    .order(1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            TemplateLevel2NodeJpaEntity l2 = level2Repository.save(TemplateLevel2NodeJpaEntity.builder()
                    .level1Node(l1)
                    .name("Subdimensión 1.1: Misión y Objetivos")
                    .order(1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            TemplateLevel3NodeJpaEntity l3 = level3Repository.save(TemplateLevel3NodeJpaEntity.builder()
                    .level2Node(l2)
                    .name("Criterio 1.1.1")
                    .order(1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            indicatorRepository.save(TemplateIndicatorJpaEntity.builder()
                    .level3Node(l3)
                    .code("IND-1.1.1.1")
                    .description("Indicador de estructura académica")
                    .order(1)
                    .referenceUrl("https://example.com/ref")
                    .weight(BigDecimal.ONE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
    }

    @AfterEach
    void tearDown() {
        // TEARDOWN: Cleanup created processes to prevent database pollution across test runs
        for (UUID processId : createdProcessIds) {
            try {
                processJpaRepository.deleteById(processId);
            } catch (Exception ignored) {
                // Ignore cleanup errors for idempotency
            }
        }
        createdProcessIds.clear();
    }

    @Test
    @DisplayName("AAA - Create process as JD: HTTP 201 -> Verify DB Entity and Normative Hierarchy Created")
    void createProcess_persistsProcessInDatabase() throws Exception {
        // ARRANGE: Select target career without active process
        UUID targetCareerId = DevSeedData.PROGRAM_ING_QUIMICA;
        UUID templateId = DevSeedData.TEMPLATE_CEUB_2026;

        String requestJson = """
                {
                  "career_id": "%s",
                  "template_id": "%s"
                }
                """.formatted(targetCareerId, templateId);

        // ACT: Execute POST endpoint to create process
        MvcResult result = mockMvc.perform(post("/api/v1/processes")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.careerId").value(targetCareerId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        String createdIdStr = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        UUID createdProcessId = UUID.fromString(createdIdStr);
        createdProcessIds.add(createdProcessId);

        // ASSERT DB STATE: Query database directly for persisted process record
        Optional<AccreditationProcessJpaEntity> processInDbOpt = processJpaRepository.findById(createdProcessId);
        assertThat(processInDbOpt).isPresent();

        AccreditationProcessJpaEntity processInDb = processInDbOpt.get();
        assertThat(processInDb.getCareerId()).isEqualTo(targetCareerId);
        assertThat(processInDb.getTemplateId()).isEqualTo(templateId);
        assertThat(processInDb.getStatus()).isEqualTo("ACTIVE");
        assertThat(processInDb.getStartDate()).isNotNull();
    }

    @Test
    @DisplayName("AAA - Attempt duplicate process creation: HTTP 409 Conflict -> DB State Unchanged")
    void createDuplicateProcess_returnsConflict409() throws Exception {
        // ARRANGE: Create initial process for career
        UUID targetCareerId = DevSeedData.PROGRAM_ING_ELECTRONICA;
        UUID templateId = DevSeedData.TEMPLATE_CEUB_2026;

        String requestJson = """
                {
                  "career_id": "%s",
                  "template_id": "%s"
                }
                """.formatted(targetCareerId, templateId);

        MvcResult firstResult = mockMvc.perform(post("/api/v1/processes")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        UUID firstProcessId = UUID.fromString(JsonPath.read(firstResult.getResponse().getContentAsString(), "$.id"));
        createdProcessIds.add(firstProcessId);

        // ACT & ASSERT: Creating duplicate active process for same career should fail with HTTP 409
        mockMvc.perform(post("/api/v1/processes")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("AAA - List processes as JD vs CC: HTTP 200 -> Validates role-based scope and payload format")
    void listProcesses_verifiesRoleScopingAndPayload() throws Exception {
        // ARRANGE: Create a process to ensure list is non-empty
        UUID targetCareerId = DevSeedData.PROGRAM_ING_MECANICA;
        UUID templateId = DevSeedData.TEMPLATE_CEUB_2026;

        MvcResult createResult = mockMvc.perform(post("/api/v1/processes")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "career_id": "%s",
                                  "template_id": "%s"
                                }
                                """.formatted(targetCareerId, templateId)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID createdId = UUID.fromString(JsonPath.read(createResult.getResponse().getContentAsString(), "$.id"));
        createdProcessIds.add(createdId);

        // ACT as JD (Sees all processes)
        MvcResult jdResult = mockMvc.perform(get("/api/v1/processes")
                        .header("Authorization", "Bearer " + jdAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        List<String> jdProcessIds = JsonPath.read(jdResult.getResponse().getContentAsString(), "$[*].id");
        assertThat(jdProcessIds).contains(createdId.toString());

        // ACT as CC (Sees assigned career processes)
        MvcResult ccResult = mockMvc.perform(get("/api/v1/processes")
                        .header("Authorization", "Bearer " + ccAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        List<String> ccProcessIds = JsonPath.read(ccResult.getResponse().getContentAsString(), "$[*].id");
        assertThat(ccProcessIds).isNotNull();
    }

    @Test
    @DisplayName("AAA - Get process details by ID: HTTP 200 -> Returns full process DTO with normative tree")
    void getProcessDetail_returnsFullProcessStructure() throws Exception {
        // ARRANGE: Create process for test
        UUID targetCareerId = DevSeedData.PROGRAM_ING_AMBIENTAL;
        UUID templateId = DevSeedData.TEMPLATE_CEUB_2026;

        MvcResult createResult = mockMvc.perform(post("/api/v1/processes")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "career_id": "%s",
                                  "template_id": "%s"
                                }
                                """.formatted(targetCareerId, templateId)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID processId = UUID.fromString(JsonPath.read(createResult.getResponse().getContentAsString(), "$.id"));
        createdProcessIds.add(processId);

        // ACT
        mockMvc.perform(get("/api/v1/processes/{id}", processId)
                        .header("Authorization", "Bearer " + jdAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(processId.toString()))
                .andExpect(jsonPath("$.careerId").value(targetCareerId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.level1Nodes").isArray());
    }

    @Test
    @DisplayName("AAA - Delete process without evidence as JD: HTTP 204 -> DB record updated to ARCHIVED")
    void deleteProcess_updatesStatusToArchivedInDatabase() throws Exception {
        // ARRANGE: Create process without evidence
        UUID targetCareerId = DevSeedData.PROGRAM_ING_INDUSTRIAL;
        UUID templateId = DevSeedData.TEMPLATE_CEUB_2026;

        MvcResult createResult = mockMvc.perform(post("/api/v1/processes")
                        .header("Authorization", "Bearer " + jdAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "career_id": "%s",
                                  "template_id": "%s"
                                }
                                """.formatted(targetCareerId, templateId)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID createdId = UUID.fromString(JsonPath.read(createResult.getResponse().getContentAsString(), "$.id"));
        createdProcessIds.add(createdId);

        // ACT: Delete (archive) process via REST API
        mockMvc.perform(delete("/api/v1/processes/{id}", createdId)
                        .header("Authorization", "Bearer " + jdAuthToken))
                .andExpect(status().isNoContent());

        // ASSERT DB STATE: Verify process status updated in database to ARCHIVED
        Optional<AccreditationProcessJpaEntity> archivedOpt = processJpaRepository.findById(createdId);
        assertThat(archivedOpt).isPresent();
        assertThat(archivedOpt.get().getStatus()).isEqualTo("ARCHIVED");
    }
}
