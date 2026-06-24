package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.DuplicateEmailException;
import com.umss.sigesa.domain.exception.InvalidEmailDomainException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.InvalidScopeException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserProgramAssignment;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserService (CreateUser) — FSD-UC-002")
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
    @DisplayName("Escenario: Alta [CC] — cuenta INACTIVE + assignment a carrera autorizada")
    void altaCc_inactivoConAssignment() {
        UUID userId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        AppUser saved = new AppUser(userId, Email.of("cc@umss.edu.bo"), Role.CC, UserStatus.INACTIVE,
                LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.save(any(), any(char[].class))).thenReturn(saved);

        var result = registerUserService.register("cc@umss.edu.bo", "CC", programId, "temp".toCharArray());

        assertEquals(UserStatus.INACTIVE, result.status());
        assertEquals(userId, result.userId());

        ArgumentCaptor<UserProgramAssignment> captor = ArgumentCaptor.forClass(UserProgramAssignment.class);
        verify(assignmentRepository).save(captor.capture());
        assertEquals(programId, captor.getValue().getProgramId());
        assertEquals(userId, captor.getValue().getUserId());
        verify(auditLogPort).logUserRegistered(userId, Email.of("cc@umss.edu.bo"));
    }

    @Test
    @DisplayName("Alta [TD] — INACTIVE sin assignment")
    void altaTd_inactivoSinAssignment() {
        UUID userId = UUID.randomUUID();
        AppUser saved = new AppUser(userId, Email.of("td@umss.edu.bo"), Role.TD, UserStatus.INACTIVE,
                LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.save(any(), any(char[].class))).thenReturn(saved);

        var result = registerUserService.register("td@umss.edu.bo", "TD", null, "temp".toCharArray());

        assertEquals(UserStatus.INACTIVE, result.status());
        verify(assignmentRepository, never()).save(any());
        verify(auditLogPort).logUserRegistered(userId, Email.of("td@umss.edu.bo"));
    }

    @Test
    @DisplayName("Alta [JD] — INACTIVE sin assignment")
    void altaJd_inactivoSinAssignment() {
        UUID userId = UUID.randomUUID();
        AppUser saved = new AppUser(userId, Email.of("jd@umss.edu.bo"), Role.JD, UserStatus.INACTIVE,
                LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.save(any(), any(char[].class))).thenReturn(saved);

        registerUserService.register("jd@umss.edu.bo", "jd", null, "temp".toCharArray());

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Email no @umss.edu.bo rechazado (FSD-BR-12)")
    void emailNoUmss_rechazado() {
        assertThrows(InvalidEmailDomainException.class,
                () -> registerUserService.register("user@gmail.com", "TD", null, "temp".toCharArray()));
    }

    @Test
    @DisplayName("[CC] sin programId — error de dominio")
    void ccSinProgramId_rechazado() {
        assertThrows(InvalidScopeException.class,
                () -> registerUserService.register("cc@umss.edu.bo", "CC", null, "temp".toCharArray()));
    }

    @Test
    @DisplayName("Rol inválido rechazado")
    void rolInvalido_rechazado() {
        assertThrows(InvalidRoleException.class,
                () -> registerUserService.register("cc@umss.edu.bo", "SUPERADMIN", null, "temp".toCharArray()));
    }

    @Test
    @DisplayName("Email duplicado rechazado con DuplicateEmailException")
    void emailDuplicado_rechazado() {
        when(userRepository.findByEmail(Email.of("cc@umss.edu.bo"))).thenReturn(Optional.of(
                new AppUser(UUID.randomUUID(), Email.of("cc@umss.edu.bo"), Role.CC, UserStatus.ACTIVE,
                        LocalDateTime.now(), LocalDateTime.now())));

        assertThrows(DuplicateEmailException.class,
                () -> registerUserService.register("cc@umss.edu.bo", "CC", UUID.randomUUID(), "temp".toCharArray()));
        verify(userRepository, never()).save(any(), any(char[].class));
    }

    @Test
    @DisplayName("Rol vacío rechazado")
    void rolVacio_rechazado() {
        assertThrows(InvalidRoleException.class,
                () -> registerUserService.register("cc@umss.edu.bo", "  ", null, "temp".toCharArray()));
    }
}
