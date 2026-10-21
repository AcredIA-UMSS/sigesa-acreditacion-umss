package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.advice.ProcessExceptionHandler;
import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.model.process.ProcessSummary;
import com.umss.sigesa.application.port.in.CreateProcessUseCase;
import com.umss.sigesa.application.port.in.DeleteProcessUseCase;
import com.umss.sigesa.application.port.in.GetPhaseProgressUseCase;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.GetProcessStatsUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.adapter.in.web.mapper.NormativeStructureWebMapper;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.application.model.process.PhaseProgress;
import com.umss.sigesa.domain.model.PhaseState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessController — consulta GET (standalone)")
class ProcessControllerQueryTest {

    @Mock
    private CreateProcessUseCase createProcessUseCase;
    @Mock
    private ListProcessesUseCase listProcessesUseCase;
    @Mock
    private GetProcessStatsUseCase getProcessStatsUseCase;
    @Mock
    private GetPhaseProgressUseCase getPhaseProgressUseCase;
    @Mock
    private GetProcessDetailUseCase getProcessDetailUseCase;
    @Mock
    private DeleteProcessUseCase deleteProcessUseCase;
    @Mock
    private UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProcessController controller = new ProcessController(
                createProcessUseCase,
                listProcessesUseCase,
                getProcessStatsUseCase,
                getProcessDetailUseCase,
                getPhaseProgressUseCase,
                deleteProcessUseCase,
                userProgramAssignmentRepositoryPort,
                new NormativeStructureWebMapper()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ProcessExceptionHandler())
                .build();
    }

    @Test
    void listReturns200() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        when(userProgramAssignmentRepositoryPort.findActiveByUserId(any())).thenReturn(List.of());
        when(listProcessesUseCase.list(any())).thenReturn(List.of(
                new ProcessSummary(processId, careerId, "INF-SIS", "Ingeniería de Sistemas",
                        templateId, "CEUB 2026", "CEUB", "CEUB", "ACTIVE", LocalDateTime.now(),
                        2, 5, null)
        ));

        mockMvc.perform(get("/api/v1/processes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(processId.toString()))
                .andExpect(jsonPath("$[0].careerCode").value("INF-SIS"))
                .andExpect(jsonPath("$[0].level1Count").value(2))
                .andExpect(jsonPath("$[0].indicatorCount").value(5));
    }

    @Test
    void getProcessNotFoundReturns404() throws Exception {
        UUID processId = UUID.randomUUID();
        when(userProgramAssignmentRepositoryPort.findActiveByUserId(any())).thenReturn(List.of());
        when(getProcessDetailUseCase.getDetail(any(), any()))
                .thenThrow(new ProcessNotFoundException(processId));

        mockMvc.perform(get("/api/v1/processes/{id}", processId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PROCESS_NOT_FOUND"));
    }

    @Test
    void getProcessDetailReturnsNormativeTree() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();
        when(userProgramAssignmentRepositoryPort.findActiveByUserId(any())).thenReturn(List.of());
        when(getProcessDetailUseCase.getDetail(any(), any())).thenReturn(
                new EnrichedProcessDetail(
                        processId,
                        UUID.randomUUID(),
                        "INF-SIS",
                        "Ingeniería de Sistemas",
                        UUID.randomUUID(),
                        "CEUB 2026",
                        "CEUB",
                        "CEUB",
                        "ACTIVE",
                        "ACTIVE",
                        LocalDateTime.now(),
                        List.of(Level1Node.builder()
                                .id(level1Id)
                                .name("Área académica")
                                .order(1)
                                .build()),
                        null
                )
        );

        mockMvc.perform(get("/api/v1/processes/{id}", processId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level1Nodes[0].name").value("Área académica"));
    }

    @Test
    void getPhaseProgressReturnsAggregate() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        when(getPhaseProgressUseCase.getProgress(processId, phaseId)).thenReturn(
                new PhaseProgress(phaseId, 5, 3, 2, 60.0, PhaseState.ABIERTA));

        mockMvc.perform(get("/api/v1/processes/{processId}/phases/{phaseId}/progress", processId, phaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phaseId").value(phaseId.toString()))
                .andExpect(jsonPath("$.totalSubphases").value(5))
                .andExpect(jsonPath("$.approvedSubphases").value(3))
                .andExpect(jsonPath("$.pendingSubphases").value(2))
                .andExpect(jsonPath("$.progressPercent").value(60.0))
                .andExpect(jsonPath("$.state").value("ABIERTA"));
    }

    @Test
    void getPhaseProgressWhenAllSubphasesAreApprovedReturns100Percent() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        when(getPhaseProgressUseCase.getProgress(processId, phaseId)).thenReturn(
                new PhaseProgress(phaseId, 2, 2, 0, 100.0, PhaseState.COMPLETADA));

        mockMvc.perform(get("/api/v1/processes/{processId}/phases/{phaseId}/progress", processId, phaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSubphases").value(2))
                .andExpect(jsonPath("$.approvedSubphases").value(2))
                .andExpect(jsonPath("$.pendingSubphases").value(0))
                .andExpect(jsonPath("$.progressPercent").value(100.0))
                .andExpect(jsonPath("$.state").value("COMPLETADA"));
    }

    @Test
    void getPhaseProgressWithoutSubphasesReturnsZeroPercent() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        when(getPhaseProgressUseCase.getProgress(processId, phaseId)).thenReturn(
                new PhaseProgress(phaseId, 0, 0, 0, 0.0, PhaseState.ABIERTA));

        mockMvc.perform(get("/api/v1/processes/{processId}/phases/{phaseId}/progress", processId, phaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSubphases").value(0))
                .andExpect(jsonPath("$.approvedSubphases").value(0))
                .andExpect(jsonPath("$.pendingSubphases").value(0))
                .andExpect(jsonPath("$.progressPercent").value(0.0));
    }

    @Test
    void getPhaseProgressWithInvalidUuidReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/processes/not-a-uuid/phases/{phaseId}/progress", UUID.randomUUID()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/processes/{processId}/phases/not-a-uuid/progress", UUID.randomUUID()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPhaseProgressNotFoundReturns404() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        when(getPhaseProgressUseCase.getProgress(processId, phaseId))
                .thenThrow(new ProcessNotFoundException(processId));

        mockMvc.perform(get("/api/v1/processes/{processId}/phases/{phaseId}/progress", processId, phaseId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PROCESS_NOT_FOUND"));
    }
}
