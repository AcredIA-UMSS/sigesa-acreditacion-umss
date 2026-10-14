package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.out.ImprovementPlanItemPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ImprovementPlanItem;
import com.umss.sigesa.domain.model.UserProgramAssignment;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListImprovementPlanServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private ImprovementPlanItemPort planItemPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    private ListImprovementPlanService service;

    @BeforeEach
    void setUp() {
        service = new ListImprovementPlanService(processQueryPort, planItemPort, assignmentRepository);
    }

    @Test
    void list_returnsItemsForVisibleProcess() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(careerId).status("ACTIVE").build()));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, careerId, LocalDateTime.now(), null)));
        when(planItemPort.findByProcessId(processId)).thenReturn(List.of(
                ImprovementPlanItem.builder().id(UUID.randomUUID()).processId(processId).objective("A").build()));

        List<ImprovementPlanItem> items = service.list(processId, userId, List.of("CC"), List.of());
        assertEquals(1, items.size());
        assertEquals("A", items.getFirst().getObjective());
    }

    @Test
    void list_unknownProcess() {
        UUID processId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.empty());
        assertThrows(ProcessNotFoundException.class, () ->
                service.list(processId, UUID.randomUUID(), List.of("TD"), List.of()));
    }
}
