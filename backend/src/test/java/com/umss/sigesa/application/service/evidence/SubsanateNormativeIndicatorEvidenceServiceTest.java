package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorEvidenceQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.EvidenceNotFoundException;
import com.umss.sigesa.domain.exception.SubsanationNotAllowedException;
import com.umss.sigesa.domain.model.IndicatorObservation;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceSubsanationCommand;
import com.umss.sigesa.domain.model.IndicatorObservationStatus;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubsanateNormativeIndicatorEvidenceService — FSD-UC-006 v2")
class SubsanateNormativeIndicatorEvidenceServiceTest {

    @Mock
    private NormativeHierarchyQueryPort hierarchyQueryPort;
    @Mock
    private NormativeIndicatorObservationPort observationPort;
    @Mock
    private NormativeIndicatorEvidenceQueryPort evidenceQueryPort;
    @Mock
    private NormativeIndicatorWorkflowPort workflowPort;
    @Mock
    private EvidenceUploadPersistencePort uploadPersistence;
    @Mock
    private EvidenceBlobStoragePort blobStorage;
    @Mock
    private ContentHashPort contentHashPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    @InjectMocks
    private SubsanateNormativeIndicatorEvidenceService service;

    @Test
    @DisplayName("Subsanación exitosa enlazada a observación OPEN")
    void subsanate_success() {
        UUID indicatorId = UUID.randomUUID();
        UUID evidenceId = UUID.randomUUID();
        UUID observationId = UUID.randomUUID();
        UUID latestVersionId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        byte[] pdf = "%PDF-1.4".getBytes();

        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), programId, UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.OBSERVADO)));
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId))
                .thenReturn(Optional.of(IndicatorObservation.builder()
                        .id(observationId)
                        .indicatorId(indicatorId)
                        .status(IndicatorObservationStatus.OPEN)
                        .build()));
        when(evidenceQueryPort.findEvidenceRef(evidenceId, indicatorId))
                .thenReturn(Optional.of(new NormativeIndicatorEvidenceQueryPort.NormativeIndicatorEvidenceRef(
                        evidenceId, indicatorId, latestVersionId, 1)));
        when(contentHashPort.sha256Hex(pdf)).thenReturn("hash-v2");
        when(blobStorage.store(evidenceId, 2, pdf, "fixed.pdf")).thenReturn("storage-v2");
        when(uploadPersistence.persistNormativeIndicatorSubsanation(
                eq(evidenceId), any(), eq(observationId), eq(1), eq(latestVersionId)))
                .thenReturn("storage-v1");

        var result = service.subsanate(new NormativeIndicatorEvidenceSubsanationCommand(
                indicatorId, evidenceId, observationId, "Corrección", pdf,
                "application/pdf", "fixed.pdf", userId));

        assertEquals(2, result.version());
        assertEquals(observationId, result.observationId());
        assertEquals(1, result.supersedesVersion());
        assertEquals(SubsanateNormativeIndicatorEvidenceService.EVENT_EVIDENCE_SUBSANATED, result.event());
        verify(workflowPort).updateIndicatorStatus(indicatorId, IndicatorState.SUBSANADO);
        verify(blobStorage).delete("storage-v1");
    }

    @Test
    @DisplayName("Rechaza subsanación sin observación OPEN")
    void subsanate_rejectsWithoutOpenObservation() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), programId, UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.SUBIDO)));
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId)).thenReturn(Optional.empty());

        assertThrows(SubsanationNotAllowedException.class, () -> service.subsanate(
                new NormativeIndicatorEvidenceSubsanationCommand(
                        indicatorId, UUID.randomUUID(), UUID.randomUUID(), "desc",
                        new byte[]{1}, "application/pdf", "f.pdf", userId)));
    }

    @Test
    @DisplayName("Rechaza observación distinta a la OPEN vigente")
    void subsanate_rejectsWrongObservationId() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID openObservationId = UUID.randomUUID();

        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), programId, UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.OBSERVADO)));
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId))
                .thenReturn(Optional.of(IndicatorObservation.builder()
                        .id(openObservationId)
                        .indicatorId(indicatorId)
                        .status(IndicatorObservationStatus.OPEN)
                        .build()));

        assertThrows(SubsanationNotAllowedException.class, () -> service.subsanate(
                new NormativeIndicatorEvidenceSubsanationCommand(
                        indicatorId, UUID.randomUUID(), UUID.randomUUID(), "desc",
                        new byte[]{1}, "application/pdf", "f.pdf", userId)));
    }

    @Test
    @DisplayName("Evidencia no pertenece al indicador")
    void subsanate_evidenceNotFound() {
        UUID indicatorId = UUID.randomUUID();
        UUID evidenceId = UUID.randomUUID();
        UUID observationId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), programId, UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.OBSERVADO)));
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId))
                .thenReturn(Optional.of(IndicatorObservation.builder()
                        .id(observationId)
                        .indicatorId(indicatorId)
                        .status(IndicatorObservationStatus.OPEN)
                        .build()));
        when(evidenceQueryPort.findEvidenceRef(evidenceId, indicatorId)).thenReturn(Optional.empty());

        assertThrows(EvidenceNotFoundException.class, () -> service.subsanate(
                new NormativeIndicatorEvidenceSubsanationCommand(
                        indicatorId, evidenceId, observationId, "desc",
                        new byte[]{1}, "application/pdf", "f.pdf", userId)));
    }
}
