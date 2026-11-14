package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.out.ImprovementPlanItemPort;
import com.umss.sigesa.application.port.out.IndicatorAnalysisPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.workflow.ProcessFreezeGuard;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.FodaQuadrant;
import com.umss.sigesa.domain.model.ImportImprovementPlanResult;
import com.umss.sigesa.domain.model.ImprovementPlanItem;
import com.umss.sigesa.domain.model.IndicatorAnalysis;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImportImprovementPlanFromFodaService — M9")
class ImportImprovementPlanFromFodaServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private ImprovementPlanItemPort planItemPort;
    @Mock
    private IndicatorAnalysisPort analysisPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;
    @Mock
    private ProcessFreezeGuard freezeGuard;

    private ImportImprovementPlanFromFodaService service;

    @BeforeEach
    void setUp() {
        service = new ImportImprovementPlanFromFodaService(
                processQueryPort, planItemPort, analysisPort, assignmentRepository, freezeGuard);
    }

    @Test
    @DisplayName("Importa solo WEAKNESS y es idempotente")
    void import_isIdempotentAndSkipsExistingSource() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID weaknessA = UUID.randomUUID();
        UUID weaknessB = UUID.randomUUID();
        UUID strengthId = UUID.randomUUID();
        stubProcess(processId, careerId, userId);

        when(analysisPort.findByProcessId(processId)).thenReturn(List.of(
                analysis(processId, weaknessA, FodaQuadrant.WEAKNESS, "Debilidad A"),
                analysis(processId, weaknessB, FodaQuadrant.WEAKNESS, "Debilidad B"),
                analysis(processId, strengthId, FodaQuadrant.STRENGTH, "Fortaleza")));
        when(planItemPort.existsByProcessIdAndSourceIndicatorId(processId, weaknessA)).thenReturn(false);
        when(planItemPort.existsByProcessIdAndSourceIndicatorId(processId, weaknessB)).thenReturn(true);
        when(planItemPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(planItemPort.findByProcessId(processId)).thenReturn(new ArrayList<>());

        ImportImprovementPlanResult first = service.importFromFoda(processId, userId, List.of("CC"), List.of());
        assertEquals(1, first.importedCount());
        assertEquals(1, first.skippedCount());
        verify(planItemPort, times(1)).save(any());

        when(planItemPort.existsByProcessIdAndSourceIndicatorId(eq(processId), any())).thenReturn(true);
        ImportImprovementPlanResult second = service.importFromFoda(processId, userId, List.of("CC"), List.of());
        assertEquals(0, second.importedCount());
        assertEquals(2, second.skippedCount());
        verify(planItemPort, times(1)).save(any());
        verify(planItemPort, never()).save(org.mockito.ArgumentMatchers.argThat(
                item -> strengthId.equals(item.getSourceIndicatorId())));
    }

    private void stubProcess(UUID processId, UUID careerId, UUID userId) {
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(careerId).status("ACTIVE").build()));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, careerId, LocalDateTime.now(), null)));
    }

    private static IndicatorAnalysis analysis(
            UUID processId, UUID indicatorId, FodaQuadrant quadrant, String synthesis) {
        return IndicatorAnalysis.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .indicatorId(indicatorId)
                .fodaQuadrant(quadrant)
                .synthesis(synthesis)
                .build();
    }
}
