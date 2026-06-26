package com.umss.sigesa.adapter.out.auth;

import com.umss.sigesa.adapter.out.persistance.AppUserJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AppUserEntity;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import com.umss.sigesa.domain.model.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalAuthAdapterTest {

    @Mock
    private AppUserJpaRepository userJpaRepository;
    @Mock
    private UserProgramAssignmentRepositoryPort assignmentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LocalAuthAdapter localAuthAdapter;

    @Test
    void authenticate_validCredentialsReturnsIdentityWithProgramScope() {
        UUID userId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        Email email = Email.of("cc@umss.edu.bo");
        AppUserEntity entity = activeEntity(userId, email.value(), Role.CC, "hash");

        when(userJpaRepository.findByEmail(email.value())).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(assignmentRepository.findActiveByUserId(userId)).thenReturn(List.of(
                new UserProgramAssignment(UUID.randomUUID(), userId, programId, LocalDateTime.now(), null)
        ));

        Optional<AuthenticatedIdentity> result = localAuthAdapter.authenticate(email, "secret".toCharArray());

        assertTrue(result.isPresent());
        assertEquals(Role.CC, result.get().role());
        assertEquals(List.of(programId), result.get().programScope());
    }

    @Test
    void authenticate_wrongPasswordReturnsEmpty() {
        Email email = Email.of("cc@umss.edu.bo");
        AppUserEntity entity = activeEntity(UUID.randomUUID(), email.value(), Role.CC, "hash");

        when(userJpaRepository.findByEmail(email.value())).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertTrue(localAuthAdapter.authenticate(email, "bad".toCharArray()).isEmpty());
    }

    @Test
    void authenticate_deactivatedUserReturnsEmpty() {
        Email email = Email.of("cc@umss.edu.bo");
        AppUserEntity entity = activeEntity(UUID.randomUUID(), email.value(), Role.CC, "hash");
        entity.setStatus(UserStatus.DEACTIVATED);

        when(userJpaRepository.findByEmail(email.value())).thenReturn(Optional.of(entity));

        assertTrue(localAuthAdapter.authenticate(email, "secret".toCharArray()).isEmpty());
    }

    @Test
    void authenticate_missingUserReturnsEmpty() {
        Email email = Email.of("ghost@umss.edu.bo");
        when(userJpaRepository.findByEmail(email.value())).thenReturn(Optional.empty());

        assertTrue(localAuthAdapter.authenticate(email, "secret".toCharArray()).isEmpty());
    }

    private AppUserEntity activeEntity(UUID id, String email, Role role, String hash) {
        AppUserEntity entity = new AppUserEntity();
        entity.setId(id);
        entity.setEmail(email);
        entity.setRole(role);
        entity.setStatus(UserStatus.ACTIVE);
        entity.setPasswordHash(hash);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }
}
