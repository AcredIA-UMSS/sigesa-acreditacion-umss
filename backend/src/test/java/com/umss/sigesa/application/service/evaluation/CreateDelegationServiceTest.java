package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.out.IndicatorDelegationPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.InvalidCaeUserException;
import com.umss.sigesa.domain.exception.ValidationException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.IndicatorDelegation;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import com.umss.sigesa.domain.model.UserStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDelegationServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private IndicatorDelegationPort delegationPort;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    private CreateDelegationService service;

    @BeforeEach
    void setUp() {
        service = new CreateDelegationService(
                processQueryPort, delegationPort, userRepository, assignmentRepository);
    }

    @Test
    void create_rejectsNonCaeUser() {
        UUID processId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        stubProcess(processId, requesterId);
        when(userRepository.findById(targetId)).thenReturn(Optional.of(
                new AppUser(targetId, Email.of("cc@umss.edu.bo"), Role.CC, UserStatus.ACTIVE,
                        LocalDateTime.now(), LocalDateTime.now())));

        assertThrows(InvalidCaeUserException.class, () -> service.create(
                processId, targetId, UUID.randomUUID(), null, null,
                requesterId, List.of("CC"), List.of()));
    }

    @Test
    void create_requiresExactlyOneScope() {
        UUID processId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        stubProcess(processId, requesterId);
        when(userRepository.findById(targetId)).thenReturn(Optional.of(
                new AppUser(targetId, Email.of("cae@umss.edu.bo"), Role.CAE_MEMBER, UserStatus.ACTIVE,
                        LocalDateTime.now(), LocalDateTime.now())));

        assertThrows(ValidationException.class, () -> service.create(
                processId, targetId, null, null, null,
                requesterId, List.of("CC"), List.of()));
    }

    @Test
    void create_savesIndicatorScope() {
        UUID processId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();
        stubProcess(processId, requesterId);
        when(userRepository.findById(targetId)).thenReturn(Optional.of(
                new AppUser(targetId, Email.of("cae@umss.edu.bo"), Role.CAE_MEMBER, UserStatus.ACTIVE,
                        LocalDateTime.now(), LocalDateTime.now())));
        when(delegationPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        IndicatorDelegation saved = service.create(
                processId, targetId, null, null, indicatorId,
                requesterId, List.of("CC"), List.of());

        assertEquals(indicatorId, saved.getIndicatorId());
        assertEquals(targetId, saved.getUserId());
    }

    private void stubProcess(UUID processId, UUID requesterId) {
        UUID careerId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder().id(processId).careerId(careerId).status("ACTIVE").build()));
        when(assignmentRepository.findActiveByUserId(requesterId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), requesterId, careerId, LocalDateTime.now(), null)));
    }
}
