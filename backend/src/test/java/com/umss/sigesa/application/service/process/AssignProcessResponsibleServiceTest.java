package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.ProcessResponsibleInfo;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.CareerScopeMismatchException;
import com.umss.sigesa.domain.exception.CcAlreadyAssignedToProcessException;
import com.umss.sigesa.domain.exception.InvalidResponsibleUserException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.ProcessResponsibleAssignment;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import com.umss.sigesa.domain.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignProcessResponsibleServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepositoryPort;
    @Mock
    private ProcessResponsiblePort processResponsiblePort;
    @Mock
    private ProcessStructureGuard processStructureGuard;

    private AssignProcessResponsibleService service;

    private final UUID processId = UUID.randomUUID();
    private final UUID careerId = UUID.randomUUID();
    private final UUID ccUserId = UUID.randomUUID();
    private final UUID jdUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new AssignProcessResponsibleService(
                processQueryPort,
                userRepositoryPort,
                assignmentRepositoryPort,
                processResponsiblePort,
                processStructureGuard);
    }

    @Test
    void shouldAssignEligibleCc() {
        stubActiveProcess();
        AppUser cc = activeCc();
        when(userRepositoryPort.findById(ccUserId)).thenReturn(Optional.of(cc));
        when(assignmentRepositoryPort.findActiveByUserId(ccUserId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), ccUserId, careerId, LocalDateTime.now(), null)));
        when(processResponsiblePort.findActiveByUserId(ccUserId)).thenReturn(Optional.empty());
        when(processResponsiblePort.save(any(ProcessResponsibleAssignment.class)))
                .thenAnswer(invocation -> {
                    ProcessResponsibleAssignment assignment = invocation.getArgument(0);
                    assignment.setId(UUID.randomUUID());
                    assignment.setAssignedAt(LocalDateTime.now());
                    return assignment;
                });

        ProcessResponsibleInfo info = service.assign(processId, ccUserId, jdUserId);

        assertEquals(ccUserId, info.userId());
        assertEquals("María Coordinadora", info.fullName());
        verify(processResponsiblePort).revokeActiveByProcessId(processId);
    }

    @Test
    void shouldRejectCcAlreadyAssignedElsewhere() {
        stubActiveProcess();
        AppUser cc = activeCc();
        UUID otherProcessId = UUID.randomUUID();
        when(userRepositoryPort.findById(ccUserId)).thenReturn(Optional.of(cc));
        when(assignmentRepositoryPort.findActiveByUserId(ccUserId))
                .thenReturn(List.of(new UserProgramAssignment(UUID.randomUUID(), ccUserId, careerId, LocalDateTime.now(), null)));
        when(processResponsiblePort.findActiveByUserId(ccUserId))
                .thenReturn(Optional.of(ProcessResponsibleAssignment.builder()
                        .processId(otherProcessId)
                        .userId(ccUserId)
                        .build()));

        assertThrows(CcAlreadyAssignedToProcessException.class,
                () -> service.assign(processId, ccUserId, jdUserId));
    }

    @Test
    void shouldRejectCareerMismatch() {
        stubActiveProcess();
        AppUser cc = activeCc();
        when(userRepositoryPort.findById(ccUserId)).thenReturn(Optional.of(cc));
        when(assignmentRepositoryPort.findActiveByUserId(ccUserId))
                .thenReturn(List.of(new UserProgramAssignment(
                        UUID.randomUUID(), ccUserId, UUID.randomUUID(), LocalDateTime.now(), null)));

        assertThrows(CareerScopeMismatchException.class,
                () -> service.assign(processId, ccUserId, jdUserId));
    }

    @Test
    void shouldRejectNonCcUser() {
        stubActiveProcess();
        AppUser jd = new AppUser(
                ccUserId,
                Email.of("jd@umss.edu.bo"),
                Role.JD,
                UserStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Carlos",
                "Jefe",
                "70000000");
        when(userRepositoryPort.findById(ccUserId)).thenReturn(Optional.of(jd));

        assertThrows(InvalidResponsibleUserException.class,
                () -> service.assign(processId, ccUserId, jdUserId));
    }

    private void stubActiveProcess() {
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder()
                        .id(processId)
                        .careerId(careerId)
                        .status("ACTIVE")
                        .startDate(LocalDateTime.now())
                        .phases(List.of())
                        .build()));
    }

    private AppUser activeCc() {
        return new AppUser(
                ccUserId,
                Email.of("cc@umss.edu.bo"),
                Role.CC,
                UserStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "María",
                "Coordinadora",
                "71111111");
    }
}
