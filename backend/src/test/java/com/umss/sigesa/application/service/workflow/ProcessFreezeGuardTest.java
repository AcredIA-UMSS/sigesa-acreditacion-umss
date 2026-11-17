package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.ProcessFreezePort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.domain.exception.ProcessFrozenException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.OperationalMode;
import com.umss.sigesa.domain.model.ProcessFreeze;
import com.umss.sigesa.domain.model.ProcessFreezeMode;
import com.umss.sigesa.domain.model.StageDeliverableCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessFreezeGuardTest {

    @Mock
    private ProcessFreezePort freezePort;
    @Mock
    private ProcessQueryPort processQueryPort;

    private ProcessFreezeGuard guard;

    @BeforeEach
    void setUp() {
        guard = new ProcessFreezeGuard(freezePort, processQueryPort);
    }

    @Test
    void assertWritable_blocksActiveAuditReadonly() {
        UUID processId = UUID.randomUUID();
        when(freezePort.findByProcessId(processId)).thenReturn(Optional.of(ProcessFreeze.builder()
                .processId(processId)
                .mode(ProcessFreezeMode.AUDIT_READONLY)
                .build()));

        assertThrows(ProcessFrozenException.class, () -> guard.assertWritable(processId));
        assertThrows(ProcessFrozenException.class, () -> guard.assertPlanWritable(processId));
    }

    @Test
    void assertWritable_blocksImprovementMonitoringExceptPlan() {
        UUID processId = UUID.randomUUID();
        when(freezePort.findByProcessId(processId)).thenReturn(Optional.empty());
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder()
                        .id(processId)
                        .operationalMode(OperationalMode.IMPROVEMENT_MONITORING)
                        .build()));

        assertThrows(ProcessFrozenException.class, () -> guard.assertWritable(processId));
        guard.assertPlanWritable(processId);
        guard.assertGenerateWritable(processId, StageDeliverableCode.IMPROVEMENT_PROGRESS_REPORT);
        assertThrows(ProcessFrozenException.class,
                () -> guard.assertGenerateWritable(processId, StageDeliverableCode.IMPROVEMENT_PLAN));
    }

    @Test
    void assertWritable_allowsActiveProcess() {
        UUID processId = UUID.randomUUID();
        when(freezePort.findByProcessId(processId)).thenReturn(Optional.empty());
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder()
                        .id(processId)
                        .operationalMode(OperationalMode.ACTIVE)
                        .build()));

        guard.assertWritable(processId);
        guard.assertPlanWritable(processId);
    }
}
