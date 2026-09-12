package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.EvidenceUnclassifiedException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.exception.SubsanationNotAllowedException;
import com.umss.sigesa.domain.model.IndicatorObservation;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceUploadCommand;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UploadNormativeIndicatorEvidenceService — FSD-UC-004 v2")
class UploadNormativeIndicatorEvidenceServiceTest {

    @Mock
    private NormativeHierarchyQueryPort hierarchyQueryPort;
    @Mock
    private NormativeIndicatorObservationPort observationPort;
    @Mock
    private NormativeIndicatorWorkflowPort workflowPort;
    @Mock
    private EvidenceUploadPersistencePort uploadPersistence;
    @Mock
    private EvidenceBlobStoragePort blobStorage;
    @Mock
    private ContentHashPort contentHashPort;
    @Mock
    private NotificationOutboxPort notificationOutbox;
    @Mock
    private AuditLogPort auditLogPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    @InjectMocks
    private UploadNormativeIndicatorEvidenceService service;

    @Test
    @DisplayName("Carga exitosa con archivo y transición a SUBIDO")
    void upload_success() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        byte[] pdf = "%PDF-1.4".getBytes();

        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), programId, UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.PENDIENTE)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId)).thenReturn(Optional.empty());
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(contentHashPort.sha256Hex(pdf)).thenReturn("abc123");
        when(blobStorage.store(any(), eq(1), eq(pdf), eq("doc.pdf"))).thenReturn("key");

        var result = service.upload(new NormativeIndicatorEvidenceUploadCommand(
                indicatorId, "Descripción válida", pdf, "application/pdf", "doc.pdf", null, userId));

        assertEquals(1, result.version());
        assertEquals("abc123", result.contentHash());
        assertEquals(IndicatorState.SUBIDO, result.indicatorState());
        assertEquals(UploadNormativeIndicatorEvidenceService.EVENT_EVIDENCE_UPLOADED, result.event());
        verify(workflowPort).updateIndicatorStatus(indicatorId, IndicatorState.SUBIDO);
        verify(uploadPersistence).persistNormativeIndicatorUpload(any(), any(), eq(null));
        verify(notificationOutbox).enqueueEvidenceUploaded(eq(indicatorId), any(), eq(programId));
    }

    @Test
    @DisplayName("Acepta application/octet-stream si la extensión es .pdf (navegador E2E)")
    void upload_acceptsOctetStreamWithPdfExtension() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        byte[] pdf = "%PDF-1.4".getBytes();

        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), programId, UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.PENDIENTE)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId)).thenReturn(Optional.empty());
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(contentHashPort.sha256Hex(pdf)).thenReturn("abc123");
        when(blobStorage.store(any(), eq(1), eq(pdf), eq("evidencia-e2e.pdf"))).thenReturn("key");

        var result = service.upload(new NormativeIndicatorEvidenceUploadCommand(
                indicatorId, "Descripción válida", pdf, "application/octet-stream", "evidencia-e2e.pdf", null, userId));

        assertEquals(IndicatorState.SUBIDO, result.indicatorState());
    }

    @Test
    @DisplayName("Rechaza carga sin archivo ni enlace externo")
    void upload_rejectsMissingPayload() {
        assertThrows(EvidenceUnclassifiedException.class, () -> service.upload(
                new NormativeIndicatorEvidenceUploadCommand(
                        UUID.randomUUID(), "desc", null, null, null, null, UUID.randomUUID())));
    }

    @Test
    @DisplayName("Rechaza carga con observación OPEN")
    void upload_rejectsOpenObservation() {
        UUID indicatorId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.OBSERVADO)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId))
                .thenReturn(Optional.of(IndicatorObservation.builder()
                        .id(UUID.randomUUID())
                        .indicatorId(indicatorId)
                        .status(IndicatorObservationStatus.OPEN)
                        .build()));

        assertThrows(SubsanationNotAllowedException.class, () -> service.upload(
                new NormativeIndicatorEvidenceUploadCommand(
                        indicatorId, "desc", new byte[]{1}, "application/pdf", "f.pdf", null, UUID.randomUUID())));
    }

    @Test
    @DisplayName("Indicador inexistente")
    void upload_indicatorNotFound() {
        UUID indicatorId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId)).thenReturn(Optional.empty());

        assertThrows(IndicatorNotFoundException.class, () -> service.upload(
                new NormativeIndicatorEvidenceUploadCommand(
                        indicatorId, "desc", new byte[]{1}, "application/pdf", "f.pdf", null, UUID.randomUUID())));
    }

    @Test
    @DisplayName("CC fuera de alcance de carrera")
    void upload_rejectsScope() {
        UUID indicatorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.PENDIENTE)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId)).thenReturn(Optional.empty());
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of());

        assertThrows(ProgramScopeDeniedException.class, () -> service.upload(
                new NormativeIndicatorEvidenceUploadCommand(
                        indicatorId, "desc", new byte[]{1}, "application/pdf", "f.pdf", null, userId)));
    }

    @Test
    @DisplayName("Compensa blob si falla persistencia")
    void upload_compensatesBlobOnPersistenceFailure() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        byte[] pdf = "%PDF-1.4".getBytes();

        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), programId, UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.PENDIENTE)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId)).thenReturn(Optional.empty());
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(contentHashPort.sha256Hex(pdf)).thenReturn("abc123");
        when(blobStorage.store(any(), eq(1), eq(pdf), eq("doc.pdf"))).thenReturn("key");
        doThrow(new RuntimeException("db down"))
                .when(uploadPersistence).persistNormativeIndicatorUpload(any(), any(), any());

        assertThrows(RuntimeException.class, () -> service.upload(new NormativeIndicatorEvidenceUploadCommand(
                indicatorId, "Descripción válida", pdf, "application/pdf", "doc.pdf", null, userId)));

        verify(blobStorage).delete("key");
    }
}
