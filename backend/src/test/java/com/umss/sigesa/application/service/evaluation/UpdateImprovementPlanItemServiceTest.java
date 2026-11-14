package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.out.ImprovementPlanItemPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.workflow.ProcessFreezeGuard;
import com.umss.sigesa.domain.exception.ImprovementItemNotFoundException;
import com.umss.sigesa.domain.exception.ValidationException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ImprovementPlanItem;
import com.umss.sigesa.domain.model.UpdateImprovementPlanItemCommand;
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
@DisplayName("UpdateImprovementPlanItemService — M9")
class UpdateImprovementPlanItemServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private ImprovementPlanItemPort planItemPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;
    @Mock
    private ProcessFreezeGuard freezeGuard;

    private UpdateImprovementPlanItemService service;

    @BeforeEach
    void setUp() {
        service = new UpdateImprovementPlanItemService(
                processQueryPort, planItemPort, assignmentRepository, freezeGuard);
    }

    @Test
    void update_keepsSourceIndicatorId() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        stubProcess(processId, careerId, userId);
        ImprovementPlanItem existing = ImprovementPlanItem.builder()
                .id(itemId)
                .processId(processId)
                .sourceIndicatorId(sourceId)
                .objective("Antes")
                .progress(10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(planItemPort.findByIdAndProcessId(itemId, processId)).thenReturn(Optional.of(existing));
        when(planItemPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ImprovementPlanItem saved = service.update(new UpdateImprovementPlanItemCommand(
                processId, itemId, "Después", "Actividad", null, null, 25, null, null,
                userId, List.of("CC"), List.of()));

        assertEquals("Después", saved.getObjective());
        assertEquals(sourceId, saved.getSourceIndicatorId());
        assertEquals(25, saved.getProgress());
    }

    @Test
    void update_notFound() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        stubProcess(processId, careerId, userId);
        when(planItemPort.findByIdAndProcessId(itemId, processId)).thenReturn(Optional.empty());

        assertThrows(ImprovementItemNotFoundException.class, () -> service.update(new UpdateImprovementPlanItemCommand(
                processId, itemId, "X", null, null, null, 0, null, null,
                userId, List.of("CC"), List.of())));
    }

    @Test
    void update_rejectsInvalidProgress() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        stubProcess(processId, careerId, userId);
        when(planItemPort.findByIdAndProcessId(itemId, processId)).thenReturn(Optional.of(
                ImprovementPlanItem.builder().id(itemId).processId(processId).objective("A").progress(0).build()));

        assertThrows(ValidationException.class, () -> service.update(new UpdateImprovementPlanItemCommand(
                processId, itemId, "A", null, null, null, -1, null, null,
                userId, List.of("CC"), List.of())));
    }

    private void stubProcess(UUID processId, UUID careerId, UUID userId) {
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(careerId).status("ACTIVE").build()));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, careerId, LocalDateTime.now(), null)));
    }
}
