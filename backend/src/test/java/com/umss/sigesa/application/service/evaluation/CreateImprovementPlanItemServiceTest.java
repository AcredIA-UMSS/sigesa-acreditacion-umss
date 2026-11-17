package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.out.ImprovementPlanItemPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.workflow.ProcessFreezeGuard;
import com.umss.sigesa.domain.exception.ValidationException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.CreateImprovementPlanItemCommand;
import com.umss.sigesa.domain.model.ImprovementPlanItem;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateImprovementPlanItemService — M9")
class CreateImprovementPlanItemServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private ImprovementPlanItemPort planItemPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;
    @Mock
    private ProcessFreezeGuard freezeGuard;

    private CreateImprovementPlanItemService service;

    @BeforeEach
    void setUp() {
        service = new CreateImprovementPlanItemService(
                processQueryPort, planItemPort, assignmentRepository, freezeGuard);
    }

    @Test
    void create_defaultsProgressToZero() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubProcess(processId, careerId, userId);
        when(planItemPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ImprovementPlanItem saved = service.create(new CreateImprovementPlanItemCommand(
                processId, "Reducir deserción", "Tutorías", null, null, null,
                userId, List.of("CC"), List.of()));

        assertEquals(0, saved.getProgress());
        assertEquals("Reducir deserción", saved.getObjective());
    }

    @Test
    void create_rejectsBlankObjective() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubProcess(processId, careerId, userId);

        assertThrows(ValidationException.class, () -> service.create(new CreateImprovementPlanItemCommand(
                processId, "  ", null, null, null, 10,
                userId, List.of("CC"), List.of())));
    }

    @Test
    void create_rejectsProgressOutOfRange() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        stubProcess(processId, careerId, userId);

        assertThrows(ValidationException.class, () -> service.create(new CreateImprovementPlanItemCommand(
                processId, "Objetivo", null, null, null, 140,
                userId, List.of("CC"), List.of())));
    }

    private void stubProcess(UUID processId, UUID careerId, UUID userId) {
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(careerId).status("ACTIVE").build()));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, careerId, LocalDateTime.now(), null)));
    }
}
