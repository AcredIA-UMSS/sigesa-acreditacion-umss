package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.advice.ProcessExceptionHandler;
import com.umss.sigesa.application.port.in.ClosePhaseUseCase;
import com.umss.sigesa.application.port.in.GetPhaseProgressUseCase;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.PhaseProgressResult;
import com.umss.sigesa.domain.model.PhaseState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PhaseWorkflowControllerTest {

    @Mock
    private ClosePhaseUseCase closePhaseUseCase;
    @Mock
    private GetPhaseProgressUseCase getPhaseProgressUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PhaseWorkflowController controller = new PhaseWorkflowController(
                closePhaseUseCase, getPhaseProgressUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ProcessExceptionHandler())
                .build();
    }

    @Test
    void returnsProgressFieldsForPartialPhase() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        when(getPhaseProgressUseCase.getProgress(processId, phaseId))
                .thenReturn(new PhaseProgressResult(phaseId, 5, 3, 2, 60.0, PhaseState.ABIERTA));

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
    void returnsProgressForPhaseWithAllSubphasesApproved() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        when(getPhaseProgressUseCase.getProgress(processId, phaseId))
                .thenReturn(new PhaseProgressResult(phaseId, 2, 2, 0, 100.0, PhaseState.COMPLETADA));

        mockMvc.perform(get("/api/v1/processes/{processId}/phases/{phaseId}/progress", processId, phaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSubphases").value(2))
                .andExpect(jsonPath("$.approvedSubphases").value(2))
                .andExpect(jsonPath("$.pendingSubphases").value(0))
                .andExpect(jsonPath("$.progressPercent").value(100.0))
                .andExpect(jsonPath("$.state").value("COMPLETADA"));
    }

    @Test
    void returnsZeroProgressForPhaseWithoutSubphases() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        when(getPhaseProgressUseCase.getProgress(processId, phaseId))
                .thenReturn(new PhaseProgressResult(phaseId, 0, 0, 0, 0.0, PhaseState.ABIERTA));

        mockMvc.perform(get("/api/v1/processes/{processId}/phases/{phaseId}/progress", processId, phaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSubphases").value(0))
                .andExpect(jsonPath("$.approvedSubphases").value(0))
                .andExpect(jsonPath("$.pendingSubphases").value(0))
                .andExpect(jsonPath("$.progressPercent").value(0.0))
                .andExpect(jsonPath("$.state").value("ABIERTA"));
    }

    @Test
    void returnsBadRequestWhenProcessIdIsNotUuid() throws Exception {
        mockMvc.perform(get("/api/v1/processes/not-a-uuid/phases/{phaseId}/progress", UUID.randomUUID()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsBadRequestWhenPhaseIdIsNotUuid() throws Exception {
        mockMvc.perform(get("/api/v1/processes/{processId}/phases/not-a-uuid/progress", UUID.randomUUID()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundWhenProcessDoesNotExist() throws Exception {
        returnsNotFoundForIds(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    void returnsNotFoundWhenPhaseDoesNotExist() throws Exception {
        returnsNotFoundForIds(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    void returnsNotFoundWhenPhaseBelongsToAnotherProcess() throws Exception {
        returnsNotFoundForIds(UUID.randomUUID(), UUID.randomUUID());
    }

    private void returnsNotFoundForIds(UUID processId, UUID phaseId) throws Exception {
        when(getPhaseProgressUseCase.getProgress(processId, phaseId))
                .thenThrow(new ProcessNotFoundException("Fase no encontrada en el proceso: " + phaseId));

        mockMvc.perform(get("/api/v1/processes/{processId}/phases/{phaseId}/progress", processId, phaseId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PROCESS_NOT_FOUND"));
    }
}
