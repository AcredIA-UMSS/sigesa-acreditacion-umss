package com.umss.sigesa.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiExportIT {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("springdoc exporta OpenAPI con carga y DELETE de evidencias")
    void exportOpenApiContract() throws Exception {
        String raw = mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode root = MAPPER.readTree(raw);
        JsonNode paths = root.get("paths");
        assertNotNull(paths, "OpenAPI sin paths");
        assertTrue(paths.has("/api/v1/indicators/{indicatorId}/evidences"),
                "Falta POST/GET evidencias por indicador");
        assertTrue(paths.has("/api/v1/evidences/{evidenceId}"),
                "Falta DELETE /api/v1/evidences/{evidenceId}");
        assertTrue(paths.get("/api/v1/evidences/{evidenceId}").has("delete"),
                "El path de evidencia no declara DELETE");

        Path committed = Path.of("..", "openapi.json").toAbsolutePath().normalize();
        Path generated = Path.of("target", "openapi.json").toAbsolutePath().normalize();
        String pretty = MAPPER.writeValueAsString(root) + "\n";
        Files.createDirectories(generated.getParent());
        Files.writeString(generated, pretty, StandardCharsets.UTF_8);

        if (Boolean.parseBoolean(System.getenv().getOrDefault("OPENAPI_WRITE", "false"))) {
            Files.writeString(committed, pretty, StandardCharsets.UTF_8);
        }
    }
}
