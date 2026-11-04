package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.config.PublicPortalSeedDataLoader;
import com.umss.sigesa.domain.exception.PublicResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicPortalGherkinIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Tag("PRD-US-016")
    @Tag("FSD-UC-016")
    @Tag("TC-PUB")
    @DisplayName("Portal sin borradores")
    void portalWithoutDrafts() throws Exception {
        String listBody = mockMvc.perform(get("/api/v1/public/programs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.slug=='inf-sis')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.slug=='med')]").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listBody).doesNotContain(PublicPortalSeedDataLoader.SLUG_ING_CIV_DRAFT);
        assertThat(listBody.toLowerCase()).doesNotContain("observacion");
        assertThat(listBody).doesNotContain("BORRADOR");

        mockMvc.perform(get("/api/v1/public/programs/" + PublicPortalSeedDataLoader.SLUG_ING_CIV_DRAFT))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(PublicResourceNotFoundException.CLIENT_MESSAGE));

        mockMvc.perform(get("/api/v1/public/programs/slug-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(PublicResourceNotFoundException.CLIENT_MESSAGE));
    }

    @Test
    @Tag("PRD-US-016")
    @Tag("FSD-UC-016")
    @Tag("TC-PUB")
    @DisplayName("Consulta de estado publicado")
    void publishedStatusByProgramName() throws Exception {
        String listBody = mockMvc.perform(get("/api/v1/public/programs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listBody).contains("Ingeniería de Sistemas");
        assertThat(listBody).contains("\"slug\":\"inf-sis\"");

        mockMvc.perform(get("/api/v1/public/programs/inf-sis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("inf-sis"))
                .andExpect(jsonPath("$.name").value("Ingeniería de Sistemas"))
                .andExpect(jsonPath("$.accreditationStatus").value("ACREDITADA"))
                .andExpect(jsonPath("$.certificateAvailable").value(true))
                .andExpect(jsonPath("$.observacion").doesNotExist())
                .andExpect(jsonPath("$.certificateUrl").doesNotExist());
    }

    @Test
    @Tag("PRD-US-020")
    @Tag("FSD-UC-016")
    @Tag("TC-PUB2")
    @DisplayName("Descarga solo si publicado")
    void certificateDownloadOnlyIfPublished() throws Exception {
        MvcResult pdf = mockMvc.perform(get("/api/v1/public/programs/inf-sis/certificate"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"certificado-inf-sis.pdf\""))
                .andExpect(header().string(PublicProgramController.HEADER_VALID_FROM, "2025-01-01"))
                .andExpect(header().string(PublicProgramController.HEADER_VALID_UNTIL, "2029-12-31"))
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                .andReturn();

        byte[] body = pdf.getResponse().getContentAsByteArray();
        assertThat(body).startsWith("%PDF".getBytes());
        assertThat(body.length).isGreaterThan(200);
        assertThat(pdf.getResponse().getHeader(HttpHeaders.LOCATION)).isNull();
        assertThat(pdf.getResponse().getContentAsString()).doesNotContain("http://");

        mockMvc.perform(get("/api/v1/public/programs/med/certificate"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(PublicResourceNotFoundException.CLIENT_MESSAGE));

        mockMvc.perform(get("/api/v1/public/programs/ing-civ/certificate"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(PublicResourceNotFoundException.CLIENT_MESSAGE));
    }
}
