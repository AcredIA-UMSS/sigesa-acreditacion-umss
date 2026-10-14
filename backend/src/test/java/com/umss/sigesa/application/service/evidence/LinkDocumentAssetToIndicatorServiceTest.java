package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorEvidenceQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.DocumentAssetNotFoundException;
import com.umss.sigesa.domain.exception.IndicatorAlreadyHasEvidenceException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.model.DocumentAsset;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.LinkDocumentAssetToIndicatorCommand;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LinkDocumentAssetToIndicatorService — M7")
class LinkDocumentAssetToIndicatorServiceTest {

    @Mock
    private NormativeHierarchyQueryPort hierarchyQueryPort;
    @Mock
    private NormativeIndicatorObservationPort observationPort;
    @Mock
    private NormativeIndicatorWorkflowPort workflowPort;
    @Mock
    private EvidenceUploadPersistencePort uploadPersistence;
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

    @InjectMocks
    private LinkDocumentAssetToIndicatorService service;

    @Test
    @DisplayName("Vincula asset del mismo proceso y deja reusedAsset=true")
    void link_success() {
        UUID indicatorId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        DocumentAsset asset = DocumentAsset.create(
                processId, "plan.pdf", "key", "abcd1234ffff", userId, LocalDateTime.now());

        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, processId, programId, UUID.randomUUID(),
                        "N1", "IND-2", "Desc", IndicatorState.PENDIENTE)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId)).thenReturn(Optional.empty());
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(documentAssetPort.findById(asset.getId())).thenReturn(Optional.of(asset));

        var result = service.link(new LinkDocumentAssetToIndicatorCommand(indicatorId, asset.getId(), userId));

        assertTrue(result.reusedAsset());
        assertEquals(asset.getId(), result.documentAssetId());
        assertEquals(asset.getFolioCode(), result.folioCode());
        assertEquals(IndicatorState.SUBIDO, result.indicatorState());
        verify(workflowPort).updateIndicatorStatus(indicatorId, IndicatorState.SUBIDO);
        verify(uploadPersistence).persistNormativeIndicatorUpload(any(), any(), eq(null), eq(asset), eq(false), any());
    }

    @Test
    @DisplayName("Asset de otro proceso → 403")
    void link_rejectsForeignProcess() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        DocumentAsset asset = DocumentAsset.create(
                UUID.randomUUID(), "otro.pdf", "key", "abcd1234ffff", userId, LocalDateTime.now());

        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), programId, UUID.randomUUID(),
                        "N1", "IND-2", "Desc", IndicatorState.PENDIENTE)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId)).thenReturn(Optional.empty());
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(documentAssetPort.findById(asset.getId())).thenReturn(Optional.of(asset));

        assertThrows(ProgramScopeDeniedException.class, () ->
                service.link(new LinkDocumentAssetToIndicatorCommand(indicatorId, asset.getId(), userId)));
    }

    @Test
    @DisplayName("Indicador con evidencia → 409")
    void link_rejectsWhenAlreadyHasEvidence() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), programId, UUID.randomUUID(),
                        "N1", "IND-2", "Desc", IndicatorState.PENDIENTE)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId)).thenReturn(Optional.empty());
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(evidenceQueryPort.hasEvidences(indicatorId)).thenReturn(true);

        assertThrows(IndicatorAlreadyHasEvidenceException.class, () ->
                service.link(new LinkDocumentAssetToIndicatorCommand(indicatorId, UUID.randomUUID(), userId)));
    }

    @Test
    @DisplayName("Asset inexistente → 404")
    void link_assetNotFound() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), programId, UUID.randomUUID(),
                        "N1", "IND-2", "Desc", IndicatorState.PENDIENTE)));
        when(observationPort.findLatestOpenByIndicatorId(indicatorId)).thenReturn(Optional.empty());
        when(assignmentRepository.findActiveByUserId(userId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));
        when(documentAssetPort.findById(assetId)).thenReturn(Optional.empty());

        assertThrows(DocumentAssetNotFoundException.class, () ->
                service.link(new LinkDocumentAssetToIndicatorCommand(indicatorId, assetId, userId)));
    }
}
