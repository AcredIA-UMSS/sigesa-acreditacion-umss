package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorEvidenceQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.evaluation.CaeDelegationGuard;
import com.umss.sigesa.application.service.workflow.ProcessFreezeGuard;
import com.umss.sigesa.domain.exception.EvidenceUnclassifiedException;
import com.umss.sigesa.domain.exception.IndicatorAlreadyHasEvidenceException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.exception.SubsanationNotAllowedException;
import com.umss.sigesa.domain.model.DocumentAsset;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UploadNormativeIndicatorEvidenceService — FSD-UC-004 v2 / M7")
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
    @Mock
    private DocumentAssetPort documentAssetPort;
    @Mock
    private NormativeIndicatorEvidenceQueryPort evidenceQueryPort;
    @Mock
    private ProcessFreezeGuard freezeGuard;
    @Mock
    private CaeDelegationGuard caeDelegationGuard;

    @InjectMocks
    private UploadNormativeIndicatorEvidenceService service;

    @Test
    @DisplayName("Carga exitosa con archivo y transición a SUBIDO")
    void upload_success() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        byte[] pdf = "%PDF-1.4".getBytes();

        stubUploadable(indicatorId, programId, userId);
        when(contentHashPort.sha256Hex(pdf)).thenReturn("abc12345deadbeef");
        when(documentAssetPort.findByHashInProcess(any(), eq("abc12345deadbeef"))).thenReturn(Optional.empty());
        when(blobStorage.store(any(), eq(1), eq(pdf), eq("doc.pdf"))).thenReturn("key");

        var result = service.upload(new NormativeIndicatorEvidenceUploadCommand(
                indicatorId, "Descripción válida", pdf, "application/pdf", "doc.pdf", null, userId));

        assertEquals(1, result.version());
        assertEquals("abc12345deadbeef", result.contentHash());
        assertEquals(IndicatorState.SUBIDO, result.indicatorState());
        assertEquals(UploadNormativeIndicatorEvidenceService.EVENT_EVIDENCE_UPLOADED, result.event());
        assertFalse(result.reusedAsset());
        assertTrue(result.folioCode().startsWith("SIGESA-"));
        verify(workflowPort).updateIndicatorStatus(indicatorId, IndicatorState.SUBIDO);
        verify(uploadPersistence).persistNormativeIndicatorUpload(any(), any(), eq(null), any(), eq(true), any());
        verify(notificationOutbox).enqueueEvidenceUploaded(eq(indicatorId), any(), eq(programId));
    }

    @Test
    @DisplayName("Mismo hash reutiliza DocumentAsset y no almacena blob")
    void upload_reusesExistingAsset() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        byte[] pdf = "%PDF-1.4".getBytes();
        DocumentAsset existing = DocumentAsset.create(
                processId, "doc.pdf", "existing-key", "abc12345deadbeef", userId, LocalDateTime.now());

        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, processId, programId, UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.PENDIENTE)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId)).thenReturn(Optional.empty());
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(contentHashPort.sha256Hex(pdf)).thenReturn("abc12345deadbeef");
        when(documentAssetPort.findByHashInProcess(processId, "abc12345deadbeef")).thenReturn(Optional.of(existing));

        var result = service.upload(new NormativeIndicatorEvidenceUploadCommand(
                indicatorId, "Descripción válida", pdf, "application/pdf", "doc.pdf", null, userId));

        assertTrue(result.reusedAsset());
        assertEquals(existing.getId(), result.documentAssetId());
        assertEquals(existing.getFolioCode(), result.folioCode());
        verify(blobStorage, never()).store(any(), anyInt(), any(), any());
        verify(uploadPersistence).persistNormativeIndicatorUpload(any(), any(), eq(null), eq(existing), eq(false), any());
    }

    @Test
    @DisplayName("Indicador con evidencia vigente → 409")
    void upload_rejectsWhenIndicatorAlreadyHasEvidence() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubUploadable(indicatorId, programId, userId);
        when(evidenceQueryPort.hasEvidences(indicatorId)).thenReturn(true);

        assertThrows(IndicatorAlreadyHasEvidenceException.class, () -> service.upload(
                new NormativeIndicatorEvidenceUploadCommand(
                        indicatorId, "desc", new byte[]{1}, "application/pdf", "f.pdf", null, userId)));
        verify(blobStorage, never()).store(any(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Acepta application/octet-stream si la extensión es .pdf (navegador E2E)")
    void upload_acceptsOctetStreamWithPdfExtension() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        byte[] pdf = "%PDF-1.4".getBytes();

        stubUploadable(indicatorId, programId, userId);
        when(contentHashPort.sha256Hex(pdf)).thenReturn("abc123");
        when(documentAssetPort.findByHashInProcess(any(), eq("abc123"))).thenReturn(Optional.empty());
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
    @DisplayName("Permite alcance vía claim programScope del JWT si no hay fila UPA activa")
    void upload_allowsJwtProgramScopeWhenAssignmentMissing() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        byte[] pdf = "%PDF-1.4".getBytes();

        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), programId, UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.PENDIENTE)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId)).thenReturn(Optional.empty());
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of());
        when(contentHashPort.sha256Hex(pdf)).thenReturn("abc123");
        when(documentAssetPort.findByHashInProcess(any(), eq("abc123"))).thenReturn(Optional.empty());
        when(blobStorage.store(any(), eq(1), eq(pdf), eq("doc.pdf"))).thenReturn("key");

        var result = service.upload(new NormativeIndicatorEvidenceUploadCommand(
                indicatorId,
                "Descripción válida",
                pdf,
                "application/pdf",
                "doc.pdf",
                null,
                userId,
                List.of(programId)));

        assertEquals(IndicatorState.SUBIDO, result.indicatorState());
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

        stubUploadable(indicatorId, programId, userId);
        when(contentHashPort.sha256Hex(pdf)).thenReturn("abc123");
        when(documentAssetPort.findByHashInProcess(any(), eq("abc123"))).thenReturn(Optional.empty());
        when(blobStorage.store(any(), eq(1), eq(pdf), eq("doc.pdf"))).thenReturn("key");
        doThrow(new RuntimeException("db down"))
                .when(uploadPersistence).persistNormativeIndicatorUpload(any(), any(), any(), any(), anyBoolean(), any());

        assertThrows(RuntimeException.class, () -> service.upload(new NormativeIndicatorEvidenceUploadCommand(
                indicatorId, "Descripción válida", pdf, "application/pdf", "doc.pdf", null, userId)));

        verify(blobStorage).delete("key");
    }

    private void stubUploadable(UUID indicatorId, UUID programId, UUID userId) {
        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), programId, UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.PENDIENTE)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId)).thenReturn(Optional.empty());
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
    }
}
