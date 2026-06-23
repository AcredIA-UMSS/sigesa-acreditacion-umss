package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.out.AuthPort;
import com.umss.sigesa.application.port.out.IssuedToken;
import com.umss.sigesa.application.port.out.TokenPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.InvalidCredentialsException;
import com.umss.sigesa.domain.exception.RoleNotAssignedException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateServiceTest {

    @Mock
    private AuthPort authPort;
    @Mock
    private TokenPort tokenPort;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private com.umss.sigesa.application.port.out.AuditLogPort auditLogPort;

    @InjectMocks
    private AuthenticateService authenticateService;

    @Test
    void authenticate_successfulLoginActivatesInactiveUser() {
        UUID userId = UUID.randomUUID();
        Email email = Email.of("cc@umss.edu.bo");
        UUID programId = UUID.randomUUID();
        AuthenticatedIdentity identity = new AuthenticatedIdentity(userId, email, Role.CC, List.of(programId));
        AppUser inactiveUser = new AppUser(userId, email, Role.CC, UserStatus.INACTIVE,
                LocalDateTime.now(), LocalDateTime.now());

        when(authPort.authenticate(email, "secret".toCharArray())).thenReturn(Optional.of(identity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(inactiveUser));
        when(tokenPort.issue(identity)).thenReturn(new IssuedToken("token", 3600L));

        var result = authenticateService.authenticate("cc@umss.edu.bo", "secret");

        assertEquals(Role.CC, result.role());
        assertEquals(List.of(programId), result.programScope());
        verify(userRepository).update(any(AppUser.class));
    }

    @Test
    void authenticate_invalidCredentialsThrows401() {
        when(authPort.authenticate(any(Email.class), any(char[].class))).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authenticateService.authenticate("ghost@umss.edu.bo", "bad"));
    }

    @Test
    void authenticate_missingRoleThrows403() {
        UUID userId = UUID.randomUUID();
        Email email = Email.of("cc@umss.edu.bo");
        AuthenticatedIdentity identity = new AuthenticatedIdentity(userId, email, null, List.of());

        when(authPort.authenticate(email, "secret".toCharArray())).thenReturn(Optional.of(identity));

        assertThrows(RoleNotAssignedException.class,
                () -> authenticateService.authenticate("cc@umss.edu.bo", "secret"));
        verify(userRepository, never()).findById(userId);
    }
}
