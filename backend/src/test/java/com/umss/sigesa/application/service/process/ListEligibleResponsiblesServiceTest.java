package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.in.ListUsersUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessResponsibleAssignment;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListEligibleResponsiblesServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private ListUsersUseCase listUsersUseCase;
    @Mock
    private ProcessResponsiblePort processResponsiblePort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepositoryPort;

    private ListEligibleResponsiblesService service;

    private final UUID processId = UUID.randomUUID();
    private final UUID careerId = UUID.randomUUID();
    private final UUID otherCareerId = UUID.randomUUID();
    private final UUID ccOfCareer = UUID.randomUUID();
    private final UUID ccOtherCareer = UUID.randomUUID();
    private final UUID ccOccupied = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ListEligibleResponsiblesService(
                processQueryPort, listUsersUseCase, processResponsiblePort, assignmentRepositoryPort);
    }

    @Test
    void listsOnlyActiveCoordinatorsAssignedToProcessCareer() {
        stubProcess();
        when(assignmentRepositoryPort.findActiveByProgramId(careerId)).thenReturn(List.of(
                assignment(ccOfCareer, careerId),
                assignment(ccOccupied, careerId)));
        when(listUsersUseCase.list("CC", "ACTIVE")).thenReturn(List.of(
                user(ccOfCareer, "Ana Sistemas", "ana@umss.edu.bo"),
                user(ccOtherCareer, "Luis Civil", "luis@umss.edu.bo"),
                user(ccOccupied, "Carla Ocupada", "carla@umss.edu.bo")));
        when(processResponsiblePort.findActiveByProcessId(processId)).thenReturn(Optional.empty());
        when(processResponsiblePort.findUserIdsWithActiveAssignment()).thenReturn(Set.of(ccOccupied));

        var result = service.listEligible(processId);

        assertEquals(1, result.size());
        assertEquals(ccOfCareer, result.getFirst().userId());
        assertEquals("Ana Sistemas", result.getFirst().fullName());
    }

    @Test
    void includesCurrentResponsibleEvenIfOccupied() {
        stubProcess();
        when(assignmentRepositoryPort.findActiveByProgramId(careerId))
                .thenReturn(List.of(assignment(ccOccupied, careerId)));
        when(listUsersUseCase.list("CC", "ACTIVE")).thenReturn(List.of(
                user(ccOccupied, "Carla Ocupada", "carla@umss.edu.bo")));
        when(processResponsiblePort.findActiveByProcessId(processId)).thenReturn(Optional.of(
                ProcessResponsibleAssignment.builder()
                        .processId(processId)
                        .userId(ccOccupied)
                        .assignedBy(UUID.randomUUID())
                        .assignedAt(LocalDateTime.now())
                        .build()));
        when(processResponsiblePort.findUserIdsWithActiveAssignment()).thenReturn(Set.of(ccOccupied));

        var result = service.listEligible(processId);

        assertEquals(1, result.size());
        assertEquals(ccOccupied, result.getFirst().userId());
    }

    @Test
    void returnsEmptyWhenCareerHasNoCoordinators() {
        stubProcess();
        when(assignmentRepositoryPort.findActiveByProgramId(careerId)).thenReturn(List.of());
        when(listUsersUseCase.list("CC", "ACTIVE")).thenReturn(List.of(
                user(ccOtherCareer, "Luis Civil", "luis@umss.edu.bo")));
        when(processResponsiblePort.findActiveByProcessId(processId)).thenReturn(Optional.empty());
        when(processResponsiblePort.findUserIdsWithActiveAssignment()).thenReturn(Set.of());

        assertTrue(service.listEligible(processId).isEmpty());
    }

    private void stubProcess() {
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder()
                        .id(processId)
                        .careerId(careerId)
                        .status("ACTIVE")
                        .startDate(LocalDateTime.now())
                        .build()));
    }

    private UserProgramAssignment assignment(UUID userId, UUID programId) {
        return new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null);
    }

    private ListUsersUseCase.UserSummary user(UUID userId, String fullName, String email) {
        return new ListUsersUseCase.UserSummary(
                userId, email, "CC", "ACTIVE", List.of(otherCareerId), "N", "A", fullName, "70000000");
    }
}
