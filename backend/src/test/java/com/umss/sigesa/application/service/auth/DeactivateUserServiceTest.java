package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.UserNotFoundException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeactivateUserService — FSD-UC-002 A1")
class DeactivateUserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;
    @Mock
    private com.umss.sigesa.application.port.out.AuditLogPort auditLogPort;

    @InjectMocks
    private DeactivateUserService deactivateUserService;

    @Test
    @DisplayName("A1 Revocación: DEACTIVATED + revoked_at en assignments + audit")
    void revocacion_desactivaUsuarioYRevocaAssignments() {
        UUID userId = UUID.randomUUID();
        Email email = Email.of("cc@umss.edu.bo");
        AppUser user = new AppUser(userId, email, Role.CC, UserStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        deactivateUserService.deactivate(userId);

        verify(assignmentRepository).revokeAllActiveByUserId(userId);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).update(captor.capture());
        assertEquals(UserStatus.DEACTIVATED, captor.getValue().getStatus());
        verify(auditLogPort).logUserDeactivated(userId, email);
    }

    @Test
    @DisplayName("Usuario inexistente lanza UserNotFoundException")
    void usuarioInexistente_lanzaNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> deactivateUserService.deactivate(userId));
    }
}
