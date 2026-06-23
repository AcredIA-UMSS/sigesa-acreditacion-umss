package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.in.AuthenticateUseCase;
import com.umss.sigesa.application.port.out.IssuedToken;
import com.umss.sigesa.application.port.out.TokenPort;
import com.umss.sigesa.application.service.auth.support.InMemoryAuthPort;
import com.umss.sigesa.application.service.auth.support.InMemoryUserProgramAssignmentRepository;
import com.umss.sigesa.application.service.auth.support.InMemoryUserRepository;
import com.umss.sigesa.application.service.auth.support.RecordingAuditLogPort;
import com.umss.sigesa.domain.exception.DuplicateEmailException;
import com.umss.sigesa.domain.exception.InvalidCredentialsException;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import com.umss.sigesa.domain.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integración de {@link AuthenticateService}, {@link RegisterUserService} y
 * {@link DeactivateUserService} con adaptadores in-memory (sin Spring/BD).
 * Mapea escenarios Gherkin FSD-UC-001 y FSD-UC-002.
 */
class ModAuthServiceIntegrationTest {

    private InMemoryUserRepository userRepository;
    private InMemoryUserProgramAssignmentRepository assignmentRepository;
    private InMemoryAuthPort authPort;
    private RecordingAuditLogPort auditLogPort;
    private AuthenticateService authenticateService;
    private RegisterUserService registerUserService;
    private DeactivateUserService deactivateUserService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        assignmentRepository = new InMemoryUserProgramAssignmentRepository();
        authPort = new InMemoryAuthPort(userRepository, assignmentRepository);
        auditLogPort = new RecordingAuditLogPort();

        TokenPort tokenPort = identity -> new IssuedToken("jwt-" + identity.userId(), 3600L);

        authenticateService = new AuthenticateService(authPort, tokenPort, userRepository, auditLogPort);
        registerUserService = new RegisterUserService(userRepository, assignmentRepository, auditLogPort);
        deactivateUserService = new DeactivateUserService(userRepository, assignmentRepository, auditLogPort);
    }

    @Test
    @DisplayName("FSD-UC-001: Inicio de sesión exitoso con rol asignado")
    void fsdUc001_loginExitosoConRol_creaSesionJwt() {
        UUID programId = UUID.randomUUID();
        var registered = registerUserService.register(
                "cc@umss.edu.bo", "CC", programId, "ChangeMe123!".toCharArray());

        AuthenticateUseCase.LoginResult result = authenticateService.authenticate("cc@umss.edu.bo", "ChangeMe123!");

        assertEquals(Role.CC, result.role());
        assertEquals(List.of(programId), result.programScope());
        assertTrue(result.token().accessToken().startsWith("jwt-"));
        assertEquals(UserStatus.ACTIVE,
                userRepository.findById(registered.userId()).orElseThrow().getStatus());
        assertTrue(auditLogPort.events().contains("LOGIN:" + registered.userId()));
    }

    @Test
    @DisplayName("FSD-UC-001: Credenciales inválidas — mensaje genérico (A1)")
    void fsdUc001_credencialesInvalidas_mismo401UsuarioInexistenteYPasswordIncorrecto() {
        registerUserService.register("cc@umss.edu.bo", "CC", UUID.randomUUID(), "ChangeMe123!".toCharArray());

        InvalidCredentialsException ghost = assertThrows(InvalidCredentialsException.class,
                () -> authenticateService.authenticate("ghost@umss.edu.bo", "bad"));
        InvalidCredentialsException badPassword = assertThrows(InvalidCredentialsException.class,
                () -> authenticateService.authenticate("cc@umss.edu.bo", "bad"));

        assertEquals(InvalidCredentialsException.GENERIC_MESSAGE, ghost.getMessage());
        assertEquals(ghost.getMessage(), badPassword.getMessage());
    }

    @Test
    @DisplayName("FSD-UC-001 A1: Email no @umss.edu.bo — mismo mensaje genérico")
    void fsdUc001_emailNoUmss_mismo401() {
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> authenticateService.authenticate("user@gmail.com", "secret"));
        assertEquals(InvalidCredentialsException.GENERIC_MESSAGE, ex.getMessage());
    }

    @Test
    @DisplayName("FSD-UC-002: Alta de usuario con rol — cuenta INACTIVE + assignment")
    void fsdUc002_altaUsuarioCc_inactivoConAssignment() {
        UUID programId = UUID.randomUUID();

        var result = registerUserService.register(
                "nuevo.cc@umss.edu.bo", "CC", programId, "TempPass123!".toCharArray());

        assertEquals(UserStatus.INACTIVE, result.status());
        List<UserProgramAssignment> assignments = assignmentRepository.allAssignments();
        assertEquals(1, assignments.size());
        assertEquals(programId, assignments.get(0).getProgramId());
        assertEquals(result.userId(), assignments.get(0).getUserId());
        assertTrue(auditLogPort.events().contains("REGISTER:" + result.userId()));
    }

    @Test
    @DisplayName("FSD-UC-002: Email duplicado rechazado")
    void fsdUc002_emailDuplicado_rechazado() {
        registerUserService.register("nuevo.cc@umss.edu.bo", "CC", UUID.randomUUID(), "TempPass123!".toCharArray());

        assertThrows(DuplicateEmailException.class,
                () -> registerUserService.register("nuevo.cc@umss.edu.bo", "CC", UUID.randomUUID(), "TempPass123!".toCharArray()));
    }

    @Test
    @DisplayName("FSD-UC-002 A1: Revocación — login bloqueado, historial assignment preservado")
    void fsdUc002_revocacion_loginBloqueadoHistorialIntacto() {
        UUID programId = UUID.randomUUID();
        var registered = registerUserService.register(
                "cc@umss.edu.bo", "CC", programId, "ChangeMe123!".toCharArray());

        authenticateService.authenticate("cc@umss.edu.bo", "ChangeMe123!");

        deactivateUserService.deactivate(registered.userId());

        assertThrows(InvalidCredentialsException.class,
                () -> authenticateService.authenticate("cc@umss.edu.bo", "ChangeMe123!"));

        assertEquals(UserStatus.DEACTIVATED,
                userRepository.findById(registered.userId()).orElseThrow().getStatus());
        assertTrue(assignmentRepository.findActiveByUserId(registered.userId()).isEmpty());
        assertEquals(1, assignmentRepository.allAssignments().size());
        assertTrue(assignmentRepository.allAssignments().getFirst().getRevokedAt() != null);
        assertTrue(auditLogPort.events().contains("DEACTIVATE:" + registered.userId()));
    }
}
