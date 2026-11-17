package com.umss.sigesa.application.service.notification;

import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.ProcessResponsibleAssignment;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import com.umss.sigesa.domain.model.UserStatus;
import com.umss.sigesa.domain.model.notification.NotificationRecipient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationRecipientResolverServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;
    @Mock
    private ProcessResponsiblePort processResponsiblePort;

    @InjectMocks
    private NotificationRecipientResolverService resolver;

    @Test
    @DisplayName("ACTIVE + @umss.edu.bo only; deactivated and invalid skipped")
    void resolvesActiveUmssOnly() {
        UUID programId = UUID.randomUUID();
        UUID activeId = UUID.randomUUID();
        UUID deactivatedId = UUID.randomUUID();

        when(assignmentRepository.findActiveByProgramId(programId)).thenReturn(List.of(
                assignment(activeId, programId),
                assignment(deactivatedId, programId)));
        when(userRepository.findById(activeId)).thenReturn(Optional.of(user(activeId, Role.CC, UserStatus.ACTIVE, "cc@umss.edu.bo")));
        when(userRepository.findById(deactivatedId)).thenReturn(Optional.of(user(deactivatedId, Role.CC, UserStatus.DEACTIVATED, "old@umss.edu.bo")));

        List<NotificationRecipient> recipients = resolver.resolveForProgram(programId, Role.CC);

        assertThat(recipients).hasSize(1);
        assertThat(recipients.getFirst().userId()).isEqualTo(activeId);
        assertThat(recipients.getFirst().email()).isEqualTo("cc@umss.edu.bo");
    }

    @Test
    @DisplayName("CC union includes process responsible and program assignees; deduped")
    void ccUnionDedupes() {
        UUID programId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        UUID sharedId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();

        when(processResponsiblePort.findActiveByProcessId(processId))
                .thenReturn(Optional.of(ProcessResponsibleAssignment.builder()
                        .id(UUID.randomUUID())
                        .processId(processId)
                        .userId(sharedId)
                        .assignedBy(UUID.randomUUID())
                        .assignedAt(LocalDateTime.now())
                        .build()));
        when(userRepository.findById(sharedId)).thenReturn(Optional.of(user(sharedId, Role.CC, UserStatus.ACTIVE, "resp@umss.edu.bo")));
        when(assignmentRepository.findActiveByProgramId(programId)).thenReturn(List.of(
                assignment(sharedId, programId),
                assignment(otherId, programId)));
        when(userRepository.findById(otherId)).thenReturn(Optional.of(user(otherId, Role.CC, UserStatus.ACTIVE, "cc2@umss.edu.bo")));

        List<NotificationRecipient> recipients = resolver.resolveCcForProgram(programId, processId);

        assertThat(recipients).hasSize(2);
        assertThat(recipients).extracting(NotificationRecipient::userId).containsExactlyInAnyOrder(sharedId, otherId);
    }

    @Test
    @DisplayName("TD fallback to all ACTIVE TD when no program assignment")
    void tdFallback() {
        UUID programId = UUID.randomUUID();
        UUID tdId = UUID.randomUUID();
        when(assignmentRepository.findActiveByProgramId(programId)).thenReturn(List.of());
        when(userRepository.findAllFiltered(Role.TD, UserStatus.ACTIVE))
                .thenReturn(List.of(user(tdId, Role.TD, UserStatus.ACTIVE, "td@umss.edu.bo")));

        List<NotificationRecipient> recipients = resolver.resolveForProgram(programId, Role.TD);

        assertThat(recipients).hasSize(1);
        assertThat(recipients.getFirst().userId()).isEqualTo(tdId);
    }

    @Test
    @DisplayName("Empty recipient list when no ACTIVE users")
    void emptyList() {
        when(userRepository.findAllFiltered(Role.JD, UserStatus.ACTIVE)).thenReturn(List.of());
        assertThat(resolver.resolveActiveByRole(Role.JD)).isEmpty();
    }

    private static UserProgramAssignment assignment(UUID userId, UUID programId) {
        return new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null);
    }

    private static AppUser user(UUID id, Role role, UserStatus status, String email) {
        return new AppUser(id, Email.of(email), role, status, LocalDateTime.now(), LocalDateTime.now());
    }
}
