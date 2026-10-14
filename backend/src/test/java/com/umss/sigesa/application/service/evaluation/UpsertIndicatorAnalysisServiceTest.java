package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.out.IndicatorAnalysisPort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.ValidationException;
import com.umss.sigesa.domain.model.ComplianceJudgment;
import com.umss.sigesa.domain.model.FodaQuadrant;
import com.umss.sigesa.domain.model.IndicatorAnalysis;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.UpsertIndicatorAnalysisCommand;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpsertIndicatorAnalysisService — M8")
class UpsertIndicatorAnalysisServiceTest {

    @Mock
    private NormativeHierarchyQueryPort hierarchyQueryPort;
    @Mock
    private IndicatorAnalysisPort analysisPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    private UpsertIndicatorAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new UpsertIndicatorAnalysisService(hierarchyQueryPort, analysisPort, assignmentRepository);
    }

    @Test
    @DisplayName("Crea FODA completo")
    void upsert_createsCompleteAnalysis() {
        UUID indicatorId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubIndicator(indicatorId, processId, careerId);
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, careerId, LocalDateTime.now(), null)));
        when(analysisPort.findByIndicatorId(indicatorId)).thenReturn(Optional.empty());
        when(analysisPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        IndicatorAnalysis saved = service.upsert(command(
                indicatorId, userId, List.of("CC"), "WEAKNESS", "PARTIAL", "Síntesis"));

        assertEquals(FodaQuadrant.WEAKNESS, saved.getFodaQuadrant());
        assertEquals(ComplianceJudgment.PARTIAL, saved.getCompliance());
        assertEquals("Síntesis", saved.getSynthesis());
        assertEquals(processId, saved.getProcessId());
        verify(analysisPort).save(any());
    }

    @Test
    @DisplayName("Actualiza FODA existente")
    void upsert_updatesExisting() {
        UUID indicatorId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubIndicator(indicatorId, processId, careerId);
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, careerId, LocalDateTime.now(), null)));
        IndicatorAnalysis existing = IndicatorAnalysis.builder()
                .id(UUID.randomUUID())
                .indicatorId(indicatorId)
                .processId(processId)
                .fodaQuadrant(FodaQuadrant.STRENGTH)
                .compliance(ComplianceJudgment.COMPLIES)
                .synthesis("Antes")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        when(analysisPort.findByIndicatorId(indicatorId)).thenReturn(Optional.of(existing));
        when(analysisPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.upsert(command(indicatorId, userId, List.of("CC"), "THREAT", "DOES_NOT_COMPLY", "Ahora"));

        ArgumentCaptor<IndicatorAnalysis> captor = ArgumentCaptor.forClass(IndicatorAnalysis.class);
        verify(analysisPort).save(captor.capture());
        assertEquals(existing.getId(), captor.getValue().getId());
        assertEquals(FodaQuadrant.THREAT, captor.getValue().getFodaQuadrant());
        assertEquals("Ahora", captor.getValue().getSynthesis());
    }

    @Test
    @DisplayName("Síntesis vacía → VALIDATION_ERROR")
    void upsert_blankSynthesis() {
        UUID indicatorId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubIndicator(indicatorId, UUID.randomUUID(), careerId);
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, careerId, LocalDateTime.now(), null)));

        assertThrows(ValidationException.class, () -> service.upsert(
                command(indicatorId, userId, List.of("CC"), "STRENGTH", "COMPLIES", "   ")));
    }

    @Test
    @DisplayName("Indicador inexistente")
    void upsert_indicatorNotFound() {
        UUID indicatorId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId)).thenReturn(Optional.empty());
        assertThrows(IndicatorNotFoundException.class, () -> service.upsert(
                command(indicatorId, UUID.randomUUID(), List.of("CC"), "STRENGTH", "COMPLIES", "Texto")));
    }

    @Test
    @DisplayName("CC fuera de alcance")
    void upsert_scopeDenied() {
        UUID indicatorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubIndicator(indicatorId, UUID.randomUUID(), UUID.randomUUID());
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of());

        assertThrows(ProcessNotFoundException.class, () -> service.upsert(
                command(indicatorId, userId, List.of("CC"), "STRENGTH", "COMPLIES", "Texto")));
    }

    private void stubIndicator(UUID indicatorId, UUID processId, UUID careerId) {
        when(hierarchyQueryPort.findIndicatorContext(indicatorId)).thenReturn(Optional.of(
                new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, processId, careerId, UUID.randomUUID(), "N1",
                        "IND-01", "Indicador", IndicatorState.PENDIENTE)));
    }

    private static UpsertIndicatorAnalysisCommand command(
            UUID indicatorId, UUID userId, List<String> roles, String quadrant, String compliance, String synthesis) {
        return new UpsertIndicatorAnalysisCommand(
                indicatorId, quadrant, compliance, synthesis, userId, roles, List.of());
    }
}
