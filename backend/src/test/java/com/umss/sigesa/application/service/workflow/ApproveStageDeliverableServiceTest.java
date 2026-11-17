package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.domain.exception.DeliverableWithoutDocumentException;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.StageDeliverable;
import com.umss.sigesa.domain.model.StageDeliverableApprovalStatus;
import com.umss.sigesa.domain.model.StageDeliverableCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApproveStageDeliverableService — M9")
class ApproveStageDeliverableServiceTest {

    @Mock
    private MethodologicalStagePort stagePort;
    @Mock
    private com.umss.sigesa.application.port.out.PeerRoomChecklistPort checklistPort;

    private ApproveStageDeliverableService service;

    @BeforeEach
    void setUp() {
        service = new ApproveStageDeliverableService(stagePort, checklistPort);
    }

    @Test
    void approve_rejectsE4WithoutDocument() {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID deliverableId = UUID.randomUUID();
        when(stagePort.findByIdAndProcessId(stageId, processId)).thenReturn(Optional.of(
                MethodologicalStage.builder().id(stageId).processId(processId).build()));
        when(stagePort.findDeliverableById(deliverableId)).thenReturn(Optional.of(StageDeliverable.builder()
                .id(deliverableId)
                .stageId(stageId)
                .deliverableCode(StageDeliverableCode.SELF_ASSESSMENT_REPORT)
                .approvalStatus(StageDeliverableApprovalStatus.PENDING)
                .documentAssetId(null)
                .build()));

        assertThrows(DeliverableWithoutDocumentException.class, () -> service.approve(
                processId, stageId, deliverableId, UUID.randomUUID(), "TD"));
        verify(stagePort, never()).updateDeliverableApproval(any(), any(), any(), any());
    }

    @Test
    void approve_allowsE4WithDocument() {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID deliverableId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(stagePort.findByIdAndProcessId(stageId, processId)).thenReturn(Optional.of(
                MethodologicalStage.builder().id(stageId).processId(processId).build()));
        StageDeliverable pending = StageDeliverable.builder()
                .id(deliverableId)
                .stageId(stageId)
                .deliverableCode(StageDeliverableCode.IMPROVEMENT_PLAN)
                .approvalStatus(StageDeliverableApprovalStatus.PENDING)
                .documentAssetId(UUID.randomUUID())
                .build();
        StageDeliverable approved = StageDeliverable.builder()
                .id(deliverableId)
                .stageId(stageId)
                .deliverableCode(StageDeliverableCode.IMPROVEMENT_PLAN)
                .approvalStatus(StageDeliverableApprovalStatus.APPROVED)
                .documentAssetId(pending.getDocumentAssetId())
                .build();
        when(stagePort.findDeliverableById(deliverableId)).thenReturn(Optional.of(pending), Optional.of(approved));

        service.approve(processId, stageId, deliverableId, actorId, "TD");
        verify(stagePort).updateDeliverableApproval(
                deliverableId, StageDeliverableApprovalStatus.APPROVED, actorId, null);
    }

    @Test
    void approve_rejectsPeerRoomChecklistWhenIncomplete() {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID deliverableId = UUID.randomUUID();
        when(stagePort.findByIdAndProcessId(stageId, processId)).thenReturn(Optional.of(
                MethodologicalStage.builder().id(stageId).processId(processId).build()));
        when(stagePort.findDeliverableById(deliverableId)).thenReturn(Optional.of(StageDeliverable.builder()
                .id(deliverableId)
                .stageId(stageId)
                .deliverableCode(StageDeliverableCode.PEER_ROOM_CHECKLIST)
                .approvalStatus(StageDeliverableApprovalStatus.PENDING)
                .build()));
        when(checklistPort.allChecked(processId)).thenReturn(false);

        assertThrows(com.umss.sigesa.domain.exception.StageGateBlockedException.class, () -> service.approve(
                processId, stageId, deliverableId, UUID.randomUUID(), "TD"));
        verify(stagePort, never()).updateDeliverableApproval(any(), any(), any(), any());
    }

    @Test
    void approve_allowsPeerRoomChecklistWhenComplete() {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID deliverableId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(stagePort.findByIdAndProcessId(stageId, processId)).thenReturn(Optional.of(
                MethodologicalStage.builder().id(stageId).processId(processId).build()));
        StageDeliverable pending = StageDeliverable.builder()
                .id(deliverableId)
                .stageId(stageId)
                .deliverableCode(StageDeliverableCode.PEER_ROOM_CHECKLIST)
                .approvalStatus(StageDeliverableApprovalStatus.PENDING)
                .build();
        StageDeliverable approved = StageDeliverable.builder()
                .id(deliverableId)
                .stageId(stageId)
                .deliverableCode(StageDeliverableCode.PEER_ROOM_CHECKLIST)
                .approvalStatus(StageDeliverableApprovalStatus.APPROVED)
                .build();
        when(stagePort.findDeliverableById(deliverableId)).thenReturn(Optional.of(pending), Optional.of(approved));
        when(checklistPort.allChecked(processId)).thenReturn(true);

        service.approve(processId, stageId, deliverableId, actorId, "TD");
        verify(stagePort).updateDeliverableApproval(
                deliverableId, StageDeliverableApprovalStatus.APPROVED, actorId, null);
    }
}
