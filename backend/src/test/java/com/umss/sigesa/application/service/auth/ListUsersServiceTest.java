package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.InvalidFilterException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import com.umss.sigesa.domain.model.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUsersServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;

    @InjectMocks
    private ListUsersService service;

    @Test
    void shouldListUsersWithAssignedPrograms() {
        UUID userId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        AppUser user = new AppUser(userId, Email.of("cc@umss.edu.bo"), Role.CC, UserStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), "Juan", "Perez", "70123456");
        when(userRepository.findAllFiltered(Role.CC, UserStatus.ACTIVE)).thenReturn(List.of(user));
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)));

        var result = service.list("cc", "active");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().userId()).isEqualTo(userId);
        assertThat(result.getFirst().programIds()).containsExactly(programId);
        assertThat(result.getFirst().fullName()).isEqualTo("Juan Perez");
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersMatch() {
        when(userRepository.findAllFiltered(null, null)).thenReturn(List.of());

        var result = service.list(null, "  ");

        assertThat(result).isEmpty();
        verify(assignmentRepository, never()).findActiveByUserId(any());
    }

    @Test
    void shouldThrowWhenRoleFilterIsInvalid() {
        assertThatThrownBy(() -> service.list("SUPERADMIN", null))
                .isInstanceOf(InvalidRoleException.class);
        verify(userRepository, never()).findAllFiltered(any(), any());
    }

    @Test
    void shouldThrowWhenStatusFilterIsInvalid() {
        assertThatThrownBy(() -> service.list("TD", "BANNED"))
                .isInstanceOf(InvalidFilterException.class);
        verify(userRepository, never()).findAllFiltered(any(), any());
    }
}
