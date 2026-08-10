package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.InvalidUserStatusTransitionException;
import com.umss.sigesa.domain.exception.UserNotFoundException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivateUserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private AuditLogPort auditLogPort;

    @InjectMocks
    private ActivateUserService activateUserService;

    @Test
    void activate_reactivatesDeactivatedUser() {
        UUID userId = UUID.randomUUID();
        Email email = Email.of("cc@umss.edu.bo");
        AppUser user = new AppUser(
                userId,
                email,
                Role.CC,
                UserStatus.DEACTIVATED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        activateUserService.activate(userId);

        verify(userRepository).update(user);
        verify(auditLogPort).logUserActivated(userId, email);
    }

    @Test
    void activate_whenAlreadyActiveThrows() {
        UUID userId = UUID.randomUUID();
        AppUser user = new AppUser(
                userId,
                Email.of("cc@umss.edu.bo"),
                Role.CC,
                UserStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> activateUserService.activate(userId))
                .isInstanceOf(InvalidUserStatusTransitionException.class);
    }

    @Test
    void activate_whenMissingThrowsNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activateUserService.activate(userId))
                .isInstanceOf(UserNotFoundException.class);
    }
}
