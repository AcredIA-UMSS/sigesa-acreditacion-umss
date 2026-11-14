package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.application.port.out.PeerRoomChecklistPort;
import com.umss.sigesa.application.port.out.ProcessFreezePort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.AlreadyFrozenException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.OperationalMode;
import com.umss.sigesa.domain.model.PeerRoomChecklistItem;
import com.umss.sigesa.domain.model.ProcessFreeze;
import com.umss.sigesa.domain.model.ProcessFreezeMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FreezeProcessServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private ProcessFreezePort freezePort;
    @Mock
    private PeerRoomChecklistPort checklistPort;
    @Mock
    private MethodologicalStagePort stagePort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    private FreezeProcessService service;

    @BeforeEach
    void setUp() {
        service = new FreezeProcessService(
                processQueryPort, freezePort, checklistPort, stagePort, assignmentRepository);
    }

    @Test
    void freeze_createsRowAndSeedsChecklist() {
        UUID processId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        stubProcess(processId, actorId);
        when(freezePort.findByProcessId(processId)).thenReturn(Optional.empty());
        when(freezePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(checklistPort.findByProcessId(processId)).thenReturn(List.of());

        ProcessFreeze saved = service.freeze(processId, actorId, List.of("TD"), List.of());

        assertEquals(ProcessFreezeMode.AUDIT_READONLY, saved.getMode());
        assertNull(saved.getLiftedAt());
        verify(stagePort).updateProcessOperationalMode(processId, OperationalMode.AUDIT_READONLY);
        verify(checklistPort, times(4)).save(any(PeerRoomChecklistItem.class));
    }

    @Test
    void freeze_rejectsAlreadyActive() {
        UUID processId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        stubProcess(processId, actorId);
        when(freezePort.findByProcessId(processId)).thenReturn(Optional.of(ProcessFreeze.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .mode(ProcessFreezeMode.AUDIT_READONLY)
                .build()));

        assertThrows(AlreadyFrozenException.class,
                () -> service.freeze(processId, actorId, List.of("TD"), List.of()));
    }

    @Test
    void freeze_reusesLiftedRow() {
        UUID processId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        stubProcess(processId, actorId);
        ProcessFreeze previous = ProcessFreeze.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .mode(ProcessFreezeMode.IMPROVEMENT_PARTIAL)
                .liftedAt(LocalDateTime.now())
                .build();
        when(freezePort.findByProcessId(processId)).thenReturn(Optional.of(previous));
        when(freezePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(checklistPort.findByProcessId(processId)).thenReturn(PeerRoomChecklistItem.CATALOG.stream()
                .map(entry -> PeerRoomChecklistItem.seed(processId, entry, LocalDateTime.now()))
                .toList());

        ProcessFreeze saved = service.freeze(processId, actorId, List.of("TD"), List.of());
        assertEquals(previous.getId(), saved.getId());
        assertNull(saved.getLiftedAt());
        ArgumentCaptor<ProcessFreeze> captor = ArgumentCaptor.forClass(ProcessFreeze.class);
        verify(freezePort).save(captor.capture());
        assertEquals(ProcessFreezeMode.AUDIT_READONLY, captor.getValue().getMode());
    }

    private void stubProcess(UUID processId, UUID actorId) {
        UUID careerId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(careerId).status("ACTIVE").build()));
    }
}
