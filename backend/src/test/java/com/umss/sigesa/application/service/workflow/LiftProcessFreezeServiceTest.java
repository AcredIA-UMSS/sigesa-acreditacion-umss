package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.application.port.out.ProcessFreezePort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.OperationalMode;
import com.umss.sigesa.domain.model.ProcessFreeze;
import com.umss.sigesa.domain.model.ProcessFreezeMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiftProcessFreezeServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private ProcessFreezePort freezePort;
    @Mock
    private MethodologicalStagePort stagePort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    private LiftProcessFreezeService service;

    @BeforeEach
    void setUp() {
        service = new LiftProcessFreezeService(processQueryPort, freezePort, stagePort, assignmentRepository);
    }

    @Test
    void lift_toActiveKeepsRowButClearsActiveFreeze() {
        UUID processId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        stubProcess(processId, actorId);
        when(freezePort.findByProcessId(processId)).thenReturn(Optional.of(ProcessFreeze.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .mode(ProcessFreezeMode.AUDIT_READONLY)
                .build()));
        when(freezePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProcessFreeze saved = service.lift(processId, "ACTIVE", actorId, List.of("TD"), List.of());

        assertNotNull(saved.getLiftedAt());
        assertFalse(saved.isActiveAuditReadonly());
        verify(stagePort).updateProcessOperationalMode(processId, OperationalMode.ACTIVE);
    }

    @Test
    void lift_toMonitoringSetsPartialMode() {
        UUID processId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        stubProcess(processId, actorId);
        when(freezePort.findByProcessId(processId)).thenReturn(Optional.of(ProcessFreeze.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .mode(ProcessFreezeMode.AUDIT_READONLY)
                .build()));
        when(freezePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProcessFreeze saved = service.lift(
                processId, "IMPROVEMENT_MONITORING", actorId, List.of("TD"), List.of());

        assertEquals(ProcessFreezeMode.IMPROVEMENT_PARTIAL, saved.getMode());
        assertFalse(saved.isActiveAuditReadonly());
        verify(stagePort).updateProcessOperationalMode(processId, OperationalMode.IMPROVEMENT_MONITORING);
    }

    private void stubProcess(UUID processId, UUID actorId) {
        UUID careerId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(careerId).status("ACTIVE").build()));
    }
}
