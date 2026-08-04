package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.advice.ProcessExceptionHandler;
import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.model.process.ProcessSummary;
import com.umss.sigesa.application.port.in.CreateProcessUseCase;
import com.umss.sigesa.application.port.in.GetProcessDetailUseCase;
import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;
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
    private GetProcessDetailUseCase getProcessDetailUseCase;
    @Mock
    private UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProcessController controller = new ProcessController(
                createProcessUseCase,
                listProcessesUseCase,
                getProcessDetailUseCase,
                userProgramAssignmentRepositoryPort
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
                        templateId, "CEUB 2026", "CEUB", "ACTIVE", LocalDateTime.now(), 2, 5)
        ));

        mockMvc.perform(get("/api/v1/processes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(processId.toString()))
                .andExpect(jsonPath("$[0].careerCode").value("INF-SIS"))
                .andExpect(jsonPath("$[0].phaseCount").value(2));
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
    void getProcessDetailReturnsPhases() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        UUID subphaseId = UUID.randomUUID();
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
                        "ACTIVE",
                        LocalDateTime.now(),
                        List.of(Phase.builder()
                                .id(phaseId)
                                .name("Autoevaluación")
                                .order(1)
                                .subphases(List.of(Subphase.builder()
                                        .id(subphaseId)
                                        .name("Diagnóstico")
                                        .order(1)
                                        .build()))
                                .build())
                )
        );

        mockMvc.perform(get("/api/v1/processes/{id}", processId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phases[0].name").value("Autoevaluación"))
                .andExpect(jsonPath("$.phases[0].subphases[0].name").value("Diagnóstico"));
    }
}
