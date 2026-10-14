package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.MethodologicalStageCode;
import com.umss.sigesa.domain.model.MethodologicalStageStatus;
import com.umss.sigesa.domain.model.StageDeliverable;
import com.umss.sigesa.domain.model.StageDeliverableApprovalStatus;
import com.umss.sigesa.domain.model.StageDeliverableCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MethodologicalStageBootstrapperTest {

    @Mock
    private MethodologicalStagePort stagePort;

    private MethodologicalStageBootstrapper bootstrapper;

    @BeforeEach
    void setUp() {
        bootstrapper = new MethodologicalStageBootstrapper(stagePort);
    }

    @Test
    void existingStagesReceiveMissingE3Deliverables() {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        MethodologicalStage systematization = MethodologicalStage.builder()
                .id(stageId)
                .processId(processId)
                .code(MethodologicalStageCode.SYSTEMATIZATION)
                .status(MethodologicalStageStatus.PENDING)
                .deliverables(new ArrayList<>())
                .build();
        when(stagePort.findByProcessIdOrderByOrder(processId)).thenReturn(List.of(systematization));

        bootstrapper.bootstrapForProcess(processId);

        verify(stagePort, never()).saveStages(any());
        verify(stagePort).addDeliverableIfAbsent(stageId, StageDeliverableCode.FODA_FORMS_COMPLETE);
        verify(stagePort).addDeliverableIfAbsent(stageId, StageDeliverableCode.SYNTHESIS_MATRIX);
    }

    @Test
    void doesNotRecreateDeliverableAlreadyPresent() {
        UUID processId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        MethodologicalStage systematization = MethodologicalStage.builder()
                .id(stageId)
                .processId(processId)
                .code(MethodologicalStageCode.SYSTEMATIZATION)
                .status(MethodologicalStageStatus.PENDING)
                .deliverables(List.of(StageDeliverable.builder()
                        .deliverableCode(StageDeliverableCode.FODA_FORMS_COMPLETE)
                        .approvalStatus(StageDeliverableApprovalStatus.PENDING)
                        .build()))
                .build();
        when(stagePort.findByProcessIdOrderByOrder(processId)).thenReturn(List.of(systematization));

        bootstrapper.ensureMissingDeliverables(processId);

        verify(stagePort, never()).addDeliverableIfAbsent(eq(stageId), eq(StageDeliverableCode.FODA_FORMS_COMPLETE));
        verify(stagePort).addDeliverableIfAbsent(stageId, StageDeliverableCode.SYNTHESIS_MATRIX);
    }
}
