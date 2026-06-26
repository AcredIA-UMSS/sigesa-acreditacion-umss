package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.EvidenceRepositoryPort;
import com.umss.sigesa.application.port.out.EvidenceUploadLockPort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.EvidenceUnclassifiedException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.model.EvidenceUploadCommand;
import com.umss.sigesa.domain.model.Indicator;
import com.umss.sigesa.domain.model.IndicatorState;
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
@DisplayName("UploadEvidenceService — FSD-UC-004")
class UploadEvidenceServiceTest {

    @Mock
    private IndicatorRepositoryPort indicatorRepository;
    @Mock
    private EvidenceRepositoryPort evidenceRepository;
    @Mock
    private EvidenceUploadPersistencePort uploadPersistence;
    @Mock
    private EvidenceBlobStoragePort blobStorage;
    @Mock
    private ContentHashPort contentHashPort;
    @Mock
    private EvidenceUploadLockPort uploadLock;
    @Mock
    private NotificationOutboxPort notificationOutbox;
    @Mock
    private AuditLogPort auditLogPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    @InjectMocks
    private UploadEvidenceService service;

    @Test
    @DisplayName("Escenario: Carga exitosa con metadatos obligatorios")
    void upload_success() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID criterionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        byte[] pdf = "%PDF-1.4".getBytes();

        when(indicatorRepository.findById(indicatorId))
                .thenReturn(Optional.of(new Indicator(indicatorId, programId, criterionId, UUID.randomUUID())));
        when(indicatorRepository.getCurrentState(indicatorId)).thenReturn(IndicatorState.PENDIENTE);
        when(evidenceRepository.existsByIndicatorId(indicatorId)).thenReturn(false);
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(uploadLock.tryAcquire(indicatorId)).thenReturn(true);
        when(contentHashPort.sha256Hex(pdf)).thenReturn("abc123");
        when(blobStorage.store(any(), eq(1), eq(pdf), eq("doc.pdf"))).thenReturn("key");

        var result = service.upload(new EvidenceUploadCommand(
                indicatorId, criterionId, "Descripción válida", pdf,
                "application/pdf", "doc.pdf", userId));

        assertEquals(1, result.version());
        assertEquals("abc123", result.contentHash());
        assertEquals(IndicatorState.SUBIDO, result.currentState());
        assertEquals(UploadEvidenceService.EVENT_EVIDENCE_UPLOADED, result.event());
        verify(uploadPersistence).persistUpload(any(), any(), any());
        verify(notificationOutbox).enqueueEvidenceUploaded(eq(indicatorId), any(), eq(programId));
    }

    @Test
    @DisplayName("Escenario: Carga sin clasificación rechazada")
    void upload_rejectsMissingCriterion() {
        assertThrows(EvidenceUnclassifiedException.class, () -> service.upload(
                new EvidenceUploadCommand(UUID.randomUUID(), null, "desc", new byte[]{1},
                        "application/pdf", "f.pdf", UUID.randomUUID())));
    }

    @Test
    @DisplayName("Escenario: criterionId no coincide con taxonomía del indicador")
    void upload_rejectsCriterionMismatch() {
        UUID indicatorId = UUID.randomUUID();
        UUID indicatorCriterionId = UUID.randomUUID();
        UUID wrongCriterionId = UUID.randomUUID();

        when(indicatorRepository.findById(indicatorId))
                .thenReturn(Optional.of(new Indicator(indicatorId, UUID.randomUUID(), indicatorCriterionId, null)));

        assertThrows(EvidenceUnclassifiedException.class, () -> service.upload(
                new EvidenceUploadCommand(indicatorId, wrongCriterionId, "desc", new byte[]{1},
                        "application/pdf", "f.pdf", UUID.randomUUID())));
    }

    @Test
    @DisplayName("Escenario: CC fuera de alcance de carrera")
    void upload_rejectsScope() {
        UUID indicatorId = UUID.randomUUID();
        UUID criterionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(indicatorRepository.findById(indicatorId))
                .thenReturn(Optional.of(new Indicator(indicatorId, UUID.randomUUID(), criterionId, null)));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of());

        assertThrows(ProgramScopeDeniedException.class, () -> service.upload(
                new EvidenceUploadCommand(indicatorId, criterionId, "desc", new byte[]{1},
                        "application/pdf", "f.pdf", userId)));
    }

    @Test
    @DisplayName("Escenario: fallo en persistencia elimina blob almacenado")
    void upload_compensatesBlobOnPersistenceFailure() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID criterionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        byte[] pdf = "%PDF-1.4".getBytes();

        when(indicatorRepository.findById(indicatorId))
                .thenReturn(Optional.of(new Indicator(indicatorId, programId, criterionId, UUID.randomUUID())));
        when(indicatorRepository.getCurrentState(indicatorId)).thenReturn(IndicatorState.PENDIENTE);
        when(evidenceRepository.existsByIndicatorId(indicatorId)).thenReturn(false);
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(uploadLock.tryAcquire(indicatorId)).thenReturn(true);
        when(contentHashPort.sha256Hex(pdf)).thenReturn("abc123");
        when(blobStorage.store(any(), eq(1), eq(pdf), eq("doc.pdf"))).thenReturn("key");
        doThrow(new RuntimeException("db down")).when(uploadPersistence).persistUpload(any(), any(), any());

        assertThrows(RuntimeException.class, () -> service.upload(new EvidenceUploadCommand(
                indicatorId, criterionId, "Descripción válida", pdf,
                "application/pdf", "doc.pdf", userId)));

        verify(blobStorage).delete("key");
    }
}
