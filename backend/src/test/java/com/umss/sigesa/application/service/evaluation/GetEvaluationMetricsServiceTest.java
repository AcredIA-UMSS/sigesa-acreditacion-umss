package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.out.EvaluationMetricsPort;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.EvaluationMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetEvaluationMetricsService — M8")
class GetEvaluationMetricsServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private EvaluationMetricsPort evaluationMetricsPort;
    @Mock
    private MethodologicalStagePort stagePort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    private GetEvaluationMetricsService service;

    @BeforeEach
    void setUp() {
        service = new GetEvaluationMetricsService(
                processQueryPort, evaluationMetricsPort, stagePort, assignmentRepository);
    }

    @Test
    void get_computesRatios() {
        UUID processId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(UUID.randomUUID()).status("ACTIVE").build()));
        when(evaluationMetricsPort.countIndicatorsByProcessId(processId)).thenReturn(10L);
        when(evaluationMetricsPort.countIndicatorsWithEvidenceByProcessId(processId)).thenReturn(10L);
        when(evaluationMetricsPort.countFodaCompleteByProcessId(processId)).thenReturn(4L);
        when(stagePort.countPrimarySurveyBatches(processId)).thenReturn(1L);

        EvaluationMetrics metrics = service.get(processId, UUID.randomUUID(), List.of("TD"), List.of());

        assertEquals(10L, metrics.totalIndicators());
        assertEquals(10L, metrics.indicatorsWithEvidence());
        assertEquals(1.0d, metrics.evidenceCompletenessRatio());
        assertEquals(4L, metrics.fodaComplete());
        assertEquals(0.4d, metrics.fodaCompletenessRatio());
        assertEquals(1L, metrics.primarySurveyBatchCount());
    }

    @Test
    void get_zeroIndicatorsYieldsZeroRatios() {
        UUID processId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(UUID.randomUUID()).status("ACTIVE").build()));
        when(evaluationMetricsPort.countIndicatorsByProcessId(processId)).thenReturn(0L);
        when(evaluationMetricsPort.countIndicatorsWithEvidenceByProcessId(processId)).thenReturn(0L);
        when(evaluationMetricsPort.countFodaCompleteByProcessId(processId)).thenReturn(0L);
        when(stagePort.countPrimarySurveyBatches(processId)).thenReturn(0L);

        EvaluationMetrics metrics = service.get(processId, UUID.randomUUID(), List.of("JD"), List.of());
        assertEquals(0.0d, metrics.evidenceCompletenessRatio());
        assertEquals(0.0d, metrics.fodaCompletenessRatio());
    }

    @Test
    void get_processNotFound() {
        UUID processId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.empty());
        assertThrows(ProcessNotFoundException.class,
                () -> service.get(processId, UUID.randomUUID(), List.of("TD"), List.of()));
    }
}
