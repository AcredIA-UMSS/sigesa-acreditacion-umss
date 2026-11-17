package com.umss.sigesa.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import com.umss.sigesa.config.AuthDataLoader;
import com.umss.sigesa.domain.model.ExecutiveSemaphoreRules;
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
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExecutiveSemaphoreGherkinIT {

    private static final List<String> VALID_LIGHTS = List.of(
            ExecutiveSemaphoreRules.ROJO,
            ExecutiveSemaphoreRules.AMARILLO,
            ExecutiveSemaphoreRules.VERDE,
            ExecutiveSemaphoreRules.SIN_DATO);

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Tag("PRD-US-013")
    @Tag("FSD-UC-013")
    @Tag("TC-09")
    @DisplayName("Vista consolidada en menos de 2 minutos")
    void consolidatedViewUnderTwoMinutes() throws Exception {
        long started = System.nanoTime();
        String token = obtainSeedJdToken();

        MvcResult result = mockMvc.perform(get("/api/v1/dashboards/me/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executiveSection").exists())
                .andExpect(jsonPath("$.executiveSection.programTrafficLights").isArray())
                .andExpect(jsonPath("$.executiveSection.programTrafficLights[0].name").isString())
                .andExpect(jsonPath("$.executiveSection.programTrafficLights[0].faculty").exists())
                .andExpect(jsonPath("$.executiveSection.programTrafficLights[0].status").isString())
                .andReturn();

        List<String> statuses = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.executiveSection.programTrafficLights[*].status");
        assertThat(statuses).isNotEmpty();
        assertThat(statuses).allMatch(VALID_LIGHTS::contains);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        assertThat(elapsedMs).isLessThan(TimeUnit.MINUTES.toMillis(2));
    }

    private String obtainSeedJdToken() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(AuthDataLoader.SEED_JD_EMAIL, AuthDataLoader.SEED_JD_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
    }
}
