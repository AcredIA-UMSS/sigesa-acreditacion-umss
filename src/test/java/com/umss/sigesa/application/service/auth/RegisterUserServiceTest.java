package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.InvalidEmailDomainException;
import com.umss.sigesa.domain.exception.InvalidScopeException;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;
    @Mock
    private com.umss.sigesa.application.port.out.AuditLogPort auditLogPort;

    @InjectMocks
    private RegisterUserService registerUserService;

    @Test
    void register_ccUserCreatesInactiveWithAssignment() {
        UUID userId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        AppUser saved = new AppUser(userId, Email.of("cc@umss.edu.bo"), Role.CC, UserStatus.INACTIVE,
                LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.save(any(), any(char[].class))).thenReturn(saved);

        var result = registerUserService.register("cc@umss.edu.bo", Role.CC, programId, "temp".toCharArray());

        assertEquals(UserStatus.INACTIVE, result.status());
        verify(assignmentRepository).save(any());
    }

    @Test
    void register_invalidEmailDomainThrows() {
        assertThrows(InvalidEmailDomainException.class,
                () -> registerUserService.register("user@gmail.com", Role.TD, null, "temp".toCharArray()));
    }

    @Test
    void register_ccWithoutProgramIdThrows() {
        assertThrows(InvalidScopeException.class,
                () -> registerUserService.register("cc@umss.edu.bo", Role.CC, null, "temp".toCharArray()));
    }
}
