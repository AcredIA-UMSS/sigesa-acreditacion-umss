package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private AuditLogPort auditLogPort;
    @InjectMocks
    private LogoutService logoutService;

    @Test
    void recordsTheAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        Email email = Email.of("jd@umss.edu.bo");
        when(userRepository.findById(userId)).thenReturn(Optional.of(new AppUser(
                userId, email, Role.JD, UserStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now(),
                "Demo", "JD", "70000000")));

        logoutService.logout(userId);

        verify(auditLogPort).logLogout(userId, email);
    }
}
