package com.umss.sigesa.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import com.umss.sigesa.config.AuthDataLoader;
import com.umss.sigesa.config.DevSeedData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PeerVisitGherkinIT {

    private static final UUID PROCESS_ID = DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    @Tag("FSD-UC-040")
    @Tag("TC-VISIT")
    @DisplayName("Técnico programa la visita")
    void technicianSchedulesVisit() throws Exception {
        String tdToken = login(AuthDataLoader.SEED_TD_EMAIL, AuthDataLoader.SEED_TD_PASSWORD);
        mockMvc.perform(put("/api/v1/processes/{processId}/peer-visit", PROCESS_ID)
                        .header("Authorization", "Bearer " + tdToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startsOn": "2026-11-24",
                                  "endsOn": "2026-11-28",
                                  "venue": "Campus Central UMSS",
                                  "room": "Sala de Pares 3",
                                  "status": "SCHEDULED",
                                  "notes": "Semana ARCU-SUR",
                                  "items": [
                                    {
                                      "type": "DOCUMENT",
                                      "title": "Informe de autoevaluación",
                                      "done": false
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venue").value("Campus Central UMSS"))
                .andExpect(jsonPath("$.room").value("Sala de Pares 3"))
                .andExpect(jsonPath("$.items[0].type").value("DOCUMENT"));

        String eeToken = login(AuthDataLoader.SEED_EE_EMAIL, AuthDataLoader.SEED_EE_PASSWORD);
        mockMvc.perform(get("/api/v1/processes/{processId}/peer-visit", PROCESS_ID)
                        .header("Authorization", "Bearer " + eeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visit.venue").value("Campus Central UMSS"))
                .andExpect(jsonPath("$.visit.room").value("Sala de Pares 3"))
                .andExpect(jsonPath("$.visit.items[0].title").value("Informe de autoevaluación"));
    }

    @Test
    @Order(2)
    @Tag("FSD-UC-040")
    @Tag("TC-VISIT")
    @DisplayName("Rango inválido")
    void invalidDateRange() throws Exception {
        String token = login(AuthDataLoader.SEED_JD_EMAIL, AuthDataLoader.SEED_JD_PASSWORD);
        mockMvc.perform(put("/api/v1/processes/{processId}/peer-visit", PROCESS_ID)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startsOn": "2026-11-28",
                                  "endsOn": "2026-11-24",
                                  "venue": "Campus Central UMSS",
                                  "room": "Sala 1",
                                  "status": "SCHEDULED"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("INVALID_DATE_RANGE"));
    }

    @Test
    @Order(3)
    @Tag("FSD-UC-040")
    @Tag("TC-VISIT")
    @DisplayName("Visita cerrada inmutable")
    void completedVisitIsImmutable() throws Exception {
        String token = login(AuthDataLoader.SEED_TD_EMAIL, AuthDataLoader.SEED_TD_PASSWORD);
        mockMvc.perform(put("/api/v1/processes/{processId}/peer-visit", PROCESS_ID)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startsOn": "2026-11-24",
                                  "endsOn": "2026-11-28",
                                  "venue": "Campus Central UMSS",
                                  "room": "Sala de Pares 3",
                                  "status": "COMPLETED",
                                  "items": [{"type": "DOCUMENT", "title": "Informe de autoevaluación"}]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(put("/api/v1/processes/{processId}/peer-visit", PROCESS_ID)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startsOn": "2026-12-01",
                                  "endsOn": "2026-12-05",
                                  "venue": "Otra sede",
                                  "room": "Otra sala",
                                  "status": "SCHEDULED"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("VISIT_IMMUTABLE"));
    }

    @Test
    @Order(4)
    @Tag("FSD-UC-040")
    @Tag("TC-VISIT")
    @DisplayName("Coordinador no programa la visita")
    void coordinatorCannotWrite() throws Exception {
        String token = login(AuthDataLoader.SEED_CC_EMAIL, AuthDataLoader.SEED_CC_PASSWORD);
        mockMvc.perform(put("/api/v1/processes/{processId}/peer-visit", PROCESS_ID)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"venue":"No","room":"No","status":"SCHEDULED"}
                                """))
                .andExpect(status().isForbidden());
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
