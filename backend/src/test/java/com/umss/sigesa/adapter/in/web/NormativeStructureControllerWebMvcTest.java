package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtAuthenticationFilter;
import com.umss.sigesa.adapter.in.security.RestAuthenticationEntryPoint;
import com.umss.sigesa.adapter.in.security.SecurityConfig;
import com.umss.sigesa.adapter.in.web.advice.ProcessExceptionHandler;
import com.umss.sigesa.adapter.in.web.mapper.NormativeStructureWebMapper;
import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.port.in.GetNormativeIndicatorUseCase;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.NormativeStructureUseCases;
import com.umss.sigesa.application.port.out.NormativeStructurePort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.IndicatorHasEvidenceException;
import com.umss.sigesa.domain.exception.IndicatorIncompleteException;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.NormativeIndicator;
import com.umss.sigesa.domain.model.PhaseState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NormativeStructureController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        ProcessExceptionHandler.class,
        NormativeStructureWebMapper.class
})
@TestPropertySource(properties = {
        "sigesa.jwt.secret=sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
        "sigesa.jwt.expiration-seconds=3600"
})
class NormativeStructureControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetProcessDetailUseCase getProcessDetailUseCase;
    @MockitoBean
    private GetNormativeIndicatorUseCase getNormativeIndicatorUseCase;
    @MockitoBean
    private NormativeStructureUseCases normativeStructureUseCases;
    @MockitoBean
    private NormativeStructurePort normativeStructurePort;
    @MockitoBean
    private UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort;
    @MockitoBean
    private JwtTokenAdapter jwtTokenAdapter;

    @Test
    @WithMockUser(roles = "JD")
    void shouldCreateLevel1ForJd() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();

        when(userProgramAssignmentRepositoryPort.findActiveByUserId(any())).thenReturn(List.of());
        when(getProcessDetailUseCase.getDetail(eq(processId), any()))
                .thenReturn(enrichedDetail(processId, "CEUB"));
        when(normativeStructureUseCases.addLevel1(eq(processId), eq("Dimensión 1"), eq(1), eq("Desc")))
                .thenReturn(Level1Node.builder()
                        .id(level1Id)
                        .name("Dimensión 1")
                        .order(1)
                        .description("Desc")
                        .status(PhaseState.ABIERTA)
                        .build());

        mockMvc.perform(post("/api/v1/processes/{processId}/level1-nodes", processId)
                        .with(user("testjd").roles("JD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Dimensión 1","order":1,"description":"Desc"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dimensión 1"))
                .andExpect(jsonPath("$.label").value("Área"))
                .andExpect(jsonPath("$.order").value(1));
    }

    @Test
    @WithMockUser(roles = "TD")
    void shouldCreateLevel2ForTd() throws Exception {
        UUID level1Id = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        UUID level2Id = UUID.randomUUID();

        when(userProgramAssignmentRepositoryPort.findActiveByUserId(any())).thenReturn(List.of());
        when(normativeStructurePort.findProcessIdByLevel1(level1Id)).thenReturn(processId);
        when(getProcessDetailUseCase.getDetail(eq(processId), any()))
                .thenReturn(enrichedDetail(processId, "ARCU-SUR"));
        when(normativeStructureUseCases.addLevel2(eq(level1Id), eq("Criterio A"), eq(1), eq(null)))
                .thenReturn(Level2Node.builder()
                        .id(level2Id)
                        .name("Criterio A")
                        .order(1)
                        .build());

        mockMvc.perform(post("/api/v1/level1-nodes/{level1Id}/level2-nodes", level1Id)
                        .with(user("testtd").roles("TD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Criterio A","order":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Criterio A"))
                .andExpect(jsonPath("$.label").value("Componente"));
    }

    @Test
    @WithMockUser(roles = "CC")
    void shouldRejectCcMutations() throws Exception {
        UUID processId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/processes/{processId}/level1-nodes", processId)
                        .with(user("testcc").roles("CC"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Dimensión 1","order":1}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CC")
    void shouldListLevel1NodesForCc() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();

        when(userProgramAssignmentRepositoryPort.findActiveByUserId(any())).thenReturn(List.of());
        when(getProcessDetailUseCase.getDetail(eq(processId), any()))
                .thenReturn(new EnrichedProcessDetail(
                        processId,
                        UUID.randomUUID(),
                        "INF-SIS",
                        "Ingeniería de Sistemas",
                        UUID.randomUUID(),
                        "CEUB 2026",
                        "CEUB",
                        "CEUB",
                        "ACTIVE",
                        LocalDateTime.now(),
                        List.of(),
                        List.of(Level1Node.builder()
                                .id(level1Id)
                                .name("Dimensión 1")
                                .order(1)
                                .status(PhaseState.ABIERTA)
                                .build()),
                        null));

        mockMvc.perform(get("/api/v1/processes/{processId}/level1-nodes", processId)
                        .with(user("testcc").roles("CC")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dimensión 1"))
                .andExpect(jsonPath("$[0].label").value("Área"));
    }

    @Test
    @WithMockUser(roles = "JD")
    void shouldReturn409WhenDeletingIndicatorWithEvidence() throws Exception {
        UUID indicatorId = UUID.randomUUID();
        doThrow(new IndicatorHasEvidenceException(indicatorId))
                .when(normativeStructureUseCases).deleteIndicator(indicatorId);

        mockMvc.perform(delete("/api/v1/indicators/{indicatorId}", indicatorId)
                        .with(user("testjd").roles("JD")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INDICATOR_HAS_EVIDENCE"));
    }

    @Test
    @WithMockUser(roles = "JD")
    void shouldReturn400WhenIndicatorIncomplete() throws Exception {
        UUID level3Id = UUID.randomUUID();
        when(normativeStructureUseCases.addIndicator(
                eq(level3Id), any(), any(), any(), any(), any()))
                .thenThrow(new IndicatorIncompleteException("El indicador requiere referenceUrl HTTPS válido."));

        mockMvc.perform(post("/api/v1/level3-nodes/{level3Id}/indicators", level3Id)
                        .with(user("testjd").roles("JD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"IND-01","description":"Desc","weight":1,"order":1,"referenceUrl":"http://invalid"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INDICATOR_INCOMPLETE"));
    }

    @Test
    @WithMockUser(roles = "JD")
    void shouldCreateIndicator() throws Exception {
        UUID level3Id = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();

        when(normativeStructureUseCases.addIndicator(
                eq(level3Id),
                eq("IND-01"),
                eq("Descripción"),
                eq(new BigDecimal("1.5")),
                eq(1),
                eq("https://duea.umss.edu.bo/normativa/ind-01")))
                .thenReturn(NormativeIndicator.builder()
                        .id(indicatorId)
                        .code("IND-01")
                        .description("Descripción")
                        .weight(new BigDecimal("1.5"))
                        .order(1)
                        .referenceUrl("https://duea.umss.edu.bo/normativa/ind-01")
                        .build());

        mockMvc.perform(post("/api/v1/level3-nodes/{level3Id}/indicators", level3Id)
                        .with(user("testjd").roles("JD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"IND-01","description":"Descripción","weight":1.5,"order":1,"referenceUrl":"https://duea.umss.edu.bo/normativa/ind-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("IND-01"))
                .andExpect(jsonPath("$.weight").value(1.5));
    }

    private EnrichedProcessDetail enrichedDetail(UUID processId, String evaluatorModel) {
        return new EnrichedProcessDetail(
                processId,
                UUID.randomUUID(),
                "INF-SIS",
                "Ingeniería de Sistemas",
                UUID.randomUUID(),
                "Plantilla",
                evaluatorModel,
                evaluatorModel,
                "ACTIVE",
                LocalDateTime.now(),
                List.of(),
                List.of(),
                null);
    }
}
