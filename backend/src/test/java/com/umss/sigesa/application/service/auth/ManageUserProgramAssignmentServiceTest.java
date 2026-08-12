package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.in.ManageUserProgramAssignmentUseCase;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.InvalidScopeException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import com.umss.sigesa.domain.model.UserStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageUserProgramAssignmentServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;
    @Mock
    private ProgramCatalogPort programCatalogPort;

    private ManageUserProgramAssignmentService service;

    @BeforeEach
    void setUp() {
        service = new ManageUserProgramAssignmentService(
                userRepository, assignmentRepository, programCatalogPort);
    }

    @Test
    void update_revokesActiveAndCreatesNewAssignment() {
        UUID userId = UUID.randomUUID();
        UUID oldProgram = UUID.randomUUID();
        UUID newProgram = UUID.randomUUID();
        AppUser user = new AppUser(
                userId,
                Email.of("cc@umss.edu.bo"),
                Role.CC,
                UserStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Ana",
                "Pérez",
                "70000000");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(programCatalogPort.findById(newProgram)).thenReturn(
                Optional.of(new ProgramCatalogPort.ProgramEntry(newProgram, "INF", "Sistemas")));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, oldProgram, LocalDateTime.now(), null)));
        when(assignmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.assign(new ManageUserProgramAssignmentUseCase.AssignCommand(
                userId, newProgram, "UPDATE"));

        assertThat(result.revokedCount()).isEqualTo(1);
        verify(assignmentRepository).revokeAllActiveByUserId(userId);
        ArgumentCaptor<UserProgramAssignment> captor = ArgumentCaptor.forClass(UserProgramAssignment.class);
        verify(assignmentRepository).save(captor.capture());
        assertThat(captor.getValue().getProgramId()).isEqualTo(newProgram);
    }

    @Test
    void assign_rejectsJdRole() {
        UUID userId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        AppUser user = new AppUser(
                userId,
                Email.of("jd@umss.edu.bo"),
                Role.JD,
                UserStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.assign(
                new ManageUserProgramAssignmentUseCase.AssignCommand(userId, programId, "CREATE")))
                .isInstanceOf(InvalidScopeException.class);
        verify(assignmentRepository, never()).save(any());
    }
}
