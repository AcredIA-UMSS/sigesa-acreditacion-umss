package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.DraftingDocumentRendererPort;
import com.umss.sigesa.application.port.out.EvaluationMetricsPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.ImprovementPlanItemPort;
import com.umss.sigesa.application.port.out.IndicatorAnalysisPort;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.workflow.ProcessFreezeGuard;
import com.umss.sigesa.domain.exception.DeliverableImmutableException;
import com.umss.sigesa.domain.exception.InvalidDeliverableStageException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.DocumentAsset;
import com.umss.sigesa.domain.model.GeneratedStageDeliverable;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.MethodologicalStageCode;
import com.umss.sigesa.domain.model.StageDeliverable;
import com.umss.sigesa.domain.model.StageDeliverableApprovalStatus;
import com.umss.sigesa.domain.model.StageDeliverableCode;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateStageDeliverableDocumentService — M9")
class GenerateStageDeliverableDocumentServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private MethodologicalStagePort stagePort;
    @Mock
    private ImprovementPlanItemPort planItemPort;
    @Mock
    private IndicatorAnalysisPort analysisPort;
    @Mock
    private EvaluationMetricsPort evaluationMetricsPort;
    @Mock
    private DraftingDocumentRendererPort rendererPort;
    @Mock
    private ContentHashPort contentHashPort;
    @Mock
    private EvidenceBlobStoragePort blobStorage;
    @Mock
    private DocumentAssetPort documentAssetPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;
    @Mock
    private ProcessFreezeGuard freezeGuard;

    private GenerateStageDeliverableDocumentService service;

    @BeforeEach
    void setUp() {
        service = new GenerateStageDeliverableDocumentService(
                processQueryPort,
                stagePort,
                planItemPort,
                analysisPort,
                evaluationMetricsPort,
                rendererPort,
                contentHashPort,
                blobStorage,
                documentAssetPort,
                assignmentRepository,
                freezeGuard);
    }

    @Test
    void generate_returns201PayloadWhenDraftingAndPending() {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID deliverableId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubAccess(processId, userId);
        stubStage(processId, stageId, MethodologicalStageCode.DRAFTING);
        StageDeliverable deliverable = StageDeliverable.builder()
                .id(deliverableId)
                .stageId(stageId)
                .deliverableCode(StageDeliverableCode.IMPROVEMENT_PLAN)
                .approvalStatus(StageDeliverableApprovalStatus.PENDING)
                .build();
        when(stagePort.findDeliverableById(deliverableId)).thenReturn(Optional.of(deliverable));
        when(planItemPort.findByProcessId(processId)).thenReturn(List.of());
        when(rendererPort.render(any(), anyList())).thenReturn("pdf".getBytes());
        when(contentHashPort.sha256Hex(any())).thenReturn("abc12345deadbeef");
        when(documentAssetPort.findByHashInProcess(processId, "abc12345deadbeef")).thenReturn(Optional.empty());
        when(blobStorage.store(eq(deliverableId), anyInt(), any(), any())).thenReturn("key");
        when(documentAssetPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GeneratedStageDeliverable result = service.generate(
                processId, stageId, deliverableId, userId, List.of("CC"), List.of());

        assertEquals(deliverableId, result.deliverableId());
        assertEquals("Plan de Mejoras (borrador institucional)", result.title());
        verify(stagePort).updateDeliverableDocumentAsset(eq(deliverableId), any());
    }

    @Test
    void generate_rejectsApprovedDeliverable() {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID deliverableId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubAccess(processId, userId);
        stubStage(processId, stageId, MethodologicalStageCode.DRAFTING);
        when(stagePort.findDeliverableById(deliverableId)).thenReturn(Optional.of(StageDeliverable.builder()
                .id(deliverableId)
                .stageId(stageId)
                .deliverableCode(StageDeliverableCode.SELF_ASSESSMENT_REPORT)
                .approvalStatus(StageDeliverableApprovalStatus.APPROVED)
                .build()));

        assertThrows(DeliverableImmutableException.class, () -> service.generate(
                processId, stageId, deliverableId, userId, List.of("CC"), List.of()));
    }

    @Test
    void generate_allowsE7OnPostAccreditation() {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID deliverableId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubAccess(processId, userId);
        stubStage(processId, stageId, MethodologicalStageCode.POST_ACCREDITATION);
        when(stagePort.findDeliverableById(deliverableId)).thenReturn(Optional.of(StageDeliverable.builder()
                .id(deliverableId)
                .stageId(stageId)
                .deliverableCode(StageDeliverableCode.IMPROVEMENT_PROGRESS_REPORT)
                .approvalStatus(StageDeliverableApprovalStatus.PENDING)
                .build()));
        when(planItemPort.findByProcessId(processId)).thenReturn(List.of());
        when(rendererPort.render(any(), anyList())).thenReturn("pdf".getBytes());
        when(contentHashPort.sha256Hex(any())).thenReturn("e7hash");
        when(documentAssetPort.findByHashInProcess(processId, "e7hash")).thenReturn(Optional.empty());
        when(blobStorage.store(eq(deliverableId), anyInt(), any(), any())).thenReturn("key");
        when(documentAssetPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GeneratedStageDeliverable result = service.generate(
                processId, stageId, deliverableId, userId, List.of("CC"), List.of());

        assertEquals("Informe de avance del Plan de Mejoras (borrador institucional)", result.title());
        verify(stagePort).updateDeliverableDocumentAsset(eq(deliverableId), any());
    }

    @Test
    void generate_rejectsApprovedE7() {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID deliverableId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubAccess(processId, userId);
        stubStage(processId, stageId, MethodologicalStageCode.POST_ACCREDITATION);
        when(stagePort.findDeliverableById(deliverableId)).thenReturn(Optional.of(StageDeliverable.builder()
                .id(deliverableId)
                .stageId(stageId)
                .deliverableCode(StageDeliverableCode.IMPROVEMENT_PROGRESS_REPORT)
                .approvalStatus(StageDeliverableApprovalStatus.APPROVED)
                .build()));

        assertThrows(DeliverableImmutableException.class, () -> service.generate(
                processId, stageId, deliverableId, userId, List.of("CC"), List.of()));
    }

    @Test
    void generate_rejectsNonDraftingStage() {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID deliverableId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubAccess(processId, userId);
        stubStage(processId, stageId, MethodologicalStageCode.COLLECTION);
        when(stagePort.findDeliverableById(deliverableId)).thenReturn(Optional.of(StageDeliverable.builder()
                .id(deliverableId)
                .stageId(stageId)
                .deliverableCode(StageDeliverableCode.SECONDARY_EVIDENCE_COMPLETE)
                .approvalStatus(StageDeliverableApprovalStatus.PENDING)
                .build()));

        assertThrows(InvalidDeliverableStageException.class, () -> service.generate(
                processId, stageId, deliverableId, userId, List.of("CC"), List.of()));
    }

    @Test
    void generate_reusesExistingAssetByHash() {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID deliverableId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubAccess(processId, userId);
        stubStage(processId, stageId, MethodologicalStageCode.DRAFTING);
        when(stagePort.findDeliverableById(deliverableId)).thenReturn(Optional.of(StageDeliverable.builder()
                .id(deliverableId)
                .stageId(stageId)
                .deliverableCode(StageDeliverableCode.OFFICIAL_DATA_FORM)
                .approvalStatus(StageDeliverableApprovalStatus.PENDING)
                .build()));
        when(rendererPort.render(any(), anyList())).thenReturn("pdf".getBytes());
        when(contentHashPort.sha256Hex(any())).thenReturn("samehash");
        DocumentAsset existing = DocumentAsset.create(
                processId, "existente", "key", "samehash", userId, LocalDateTime.now());
        when(documentAssetPort.findByHashInProcess(processId, "samehash")).thenReturn(Optional.of(existing));

        GeneratedStageDeliverable result = service.generate(
                processId, stageId, deliverableId, userId, List.of("CC"), List.of());

        assertEquals(existing.getId(), result.documentAssetId());
        assertEquals(existing.getFolioCode(), result.folioCode());
        verify(stagePort).updateDeliverableDocumentAsset(deliverableId, existing.getId());
    }

    private void stubAccess(UUID processId, UUID userId) {
        UUID careerId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder()
                        .id(processId)
                        .careerId(careerId)
                        .templateId(UUID.randomUUID())
                        .status("ACTIVE")
                        .startDate(LocalDateTime.now())
                        .build()));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, careerId, LocalDateTime.now(), null)));
    }

    private void stubStage(UUID processId, UUID stageId, MethodologicalStageCode code) {
        when(stagePort.findByIdAndProcessId(stageId, processId)).thenReturn(Optional.of(
                MethodologicalStage.builder()
                        .id(stageId)
                        .processId(processId)
                        .code(code)
                        .build()));
    }
}
