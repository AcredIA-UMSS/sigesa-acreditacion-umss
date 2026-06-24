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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticateService — FSD-UC-001")
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
    @DisplayName("Escenario: Inicio de sesión exitoso con rol [CC] y programScope")
    void loginExitosoConRolCc_retornaJwtYActivaInactive() {
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
        assertEquals("token", result.token().accessToken());
        verify(userRepository).update(any(AppUser.class));
        verify(auditLogPort).logLogin(userId, email);
    }

    @Test
    @DisplayName("Escenario: Inicio de sesión exitoso con rol [JD] activo")
    void loginExitosoConRolJd_noReactivaUsuario() {
        UUID userId = UUID.randomUUID();
        Email email = Email.of("jd@umss.edu.bo");
        AuthenticatedIdentity identity = new AuthenticatedIdentity(userId, email, Role.JD, List.of());
        AppUser activeUser = new AppUser(userId, email, Role.JD, UserStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now());

        when(authPort.authenticate(email, "secret".toCharArray())).thenReturn(Optional.of(identity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(tokenPort.issue(identity)).thenReturn(new IssuedToken("token-jd", 86400L));

        var result = authenticateService.authenticate("jd@umss.edu.bo", "secret");

        assertEquals(Role.JD, result.role());
        verify(userRepository, never()).update(any(AppUser.class));
        verify(auditLogPort).logLogin(userId, email);
    }

    @Test
    @DisplayName("A1: Credenciales inválidas — usuario inexistente (401 genérico)")
    void credencialesInvalidas_usuarioInexistente() {
        when(authPort.authenticate(any(Email.class), any(char[].class))).thenReturn(Optional.empty());

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> authenticateService.authenticate("ghost@umss.edu.bo", "bad"));
        assertEquals(InvalidCredentialsException.GENERIC_MESSAGE, ex.getMessage());
    }

    @Test
    @DisplayName("A1: Credenciales inválidas — password incorrecto (401 genérico)")
    void credencialesInvalidas_passwordIncorrecto() {
        when(authPort.authenticate(any(Email.class), any(char[].class))).thenReturn(Optional.empty());

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> authenticateService.authenticate("cc@umss.edu.bo", "wrong"));
        assertEquals(InvalidCredentialsException.GENERIC_MESSAGE, ex.getMessage());
    }

    @Test
    @DisplayName("A1: Usuario DEACTIVATED tratado como credenciales inválidas")
    void credencialesInvalidas_usuarioDesactivadoViaAuthPort() {
        when(authPort.authenticate(any(Email.class), any(char[].class))).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authenticateService.authenticate("cc@umss.edu.bo", "secret"));
    }

    @Test
    @DisplayName("A2: Sin rol asignado — 403 ACCESS_DENIED")
    void sinRolAsignado_lanza403() {
        UUID userId = UUID.randomUUID();
        Email email = Email.of("cc@umss.edu.bo");
        AuthenticatedIdentity identity = new AuthenticatedIdentity(userId, email, null, List.of());

        when(authPort.authenticate(email, "secret".toCharArray())).thenReturn(Optional.of(identity));

        assertThrows(RoleNotAssignedException.class,
                () -> authenticateService.authenticate("cc@umss.edu.bo", "secret"));
        verify(userRepository, never()).findById(userId);
    }

    @Test
    @DisplayName("A1: Email no @umss.edu.bo — 401 genérico sin consultar AuthPort")
    void credencialesInvalidas_emailNoUmss() {
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> authenticateService.authenticate("user@gmail.com", "secret"));
        assertEquals(InvalidCredentialsException.GENERIC_MESSAGE, ex.getMessage());
        verify(authPort, never()).authenticate(any(Email.class), any(char[].class));
    }

    @Test
    @DisplayName("Password nulo o vacío — 401 sin consultar AuthPort")
    void passwordNuloOVacio_lanza401() {
        assertThrows(InvalidCredentialsException.class,
                () -> authenticateService.authenticate("cc@umss.edu.bo", null));
        assertThrows(InvalidCredentialsException.class,
                () -> authenticateService.authenticate("cc@umss.edu.bo", "  "));
        verify(authPort, never()).authenticate(any(Email.class), any(char[].class));
    }
}
