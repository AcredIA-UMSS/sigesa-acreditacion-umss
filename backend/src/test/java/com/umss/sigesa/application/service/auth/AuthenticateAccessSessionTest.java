package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.out.AccessSessionPort;
import com.umss.sigesa.application.port.out.AuthPort;
import com.umss.sigesa.application.port.out.IssuedToken;
import com.umss.sigesa.application.port.out.TokenPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.application.service.auth.support.RecordingAuditLogPort;
import com.umss.sigesa.domain.exception.InvalidCredentialsException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;
import com.umss.sigesa.domain.model.access.AccessSession;
import com.umss.sigesa.domain.model.access.AccessSessionEndReason;
import com.umss.sigesa.domain.model.access.AccessSessionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticateAccessSessionTest {

    private final RecordingAccessSessionPort sessions = new RecordingAccessSessionPort();
    private final RecordingAuditLogPort audit = new RecordingAuditLogPort();

    @Test
    void successfulLoginOpensSessionAndWritesLogin() {
        UUID userId = UUID.randomUUID();
        AuthenticateService service = serviceFor(userId, "Ana Duea");

        service.authenticate("ana.duea@umss.edu.bo", "secret");

        assertEquals(List.of("OPEN:ana.duea@umss.edu.bo:JD"), sessions.events);
        assertTrue(audit.events().contains("LOGIN:" + userId));
    }

    @Test
    void failedPasswordRecordsAttemptWithoutOpeningSession() {
        AuthenticateService service = serviceFor(UUID.randomUUID(), "Ana Duea");

        assertThrows(InvalidCredentialsException.class,
                () -> service.authenticate("ana.duea@umss.edu.bo", "bad"));

        assertEquals(List.of("FAIL:ana.duea@umss.edu.bo"), sessions.events);
        assertTrue(audit.events().isEmpty());
    }

    @Test
    void malformedEmailRecordsFailure() {
        AuthenticateService service = serviceFor(UUID.randomUUID(), "Ana Duea");

        assertThrows(InvalidCredentialsException.class,
                () -> service.authenticate("ana@gmail.com", "secret"));

        assertEquals(List.of("FAIL:ana@gmail.com"), sessions.events);
    }

    private AuthenticateService serviceFor(UUID userId, String fullName) {
        AuthPort authPort = (email, password) -> {
            if (!"secret".equals(new String(password))) {
                return Optional.empty();
            }
            return Optional.of(new AuthenticatedIdentity(userId, email, Role.JD, List.of()));
        };
        TokenPort tokenPort = identity -> new IssuedToken("token", 60);
        UserRepositoryPort users = new UserRepositoryPort() {
            @Override
            public Optional<AppUser> findById(UUID id) {
                String[] parts = fullName.split(" ", 2);
                return Optional.of(new AppUser(
                        userId, Email.forLogin("ana.duea@umss.edu.bo"), Role.JD, UserStatus.ACTIVE,
                        LocalDateTime.now(), LocalDateTime.now(), parts[0], parts[1], null));
            }

            @Override
            public Optional<AppUser> findByEmail(Email email) {
                return Optional.empty();
            }

            @Override
            public AppUser save(AppUser user, char[] rawPassword) {
                return user;
            }

            @Override
            public AppUser update(AppUser user) {
                return user;
            }

            @Override
            public java.util.List<AppUser> findAllFiltered(Role role, UserStatus status) {
                return java.util.List.of();
            }
        };
        return new AuthenticateService(authPort, tokenPort, users, audit, sessions);
    }

    private static final class RecordingAccessSessionPort implements AccessSessionPort {
        private final List<String> events = new ArrayList<>();

        @Override
        public UUID openSuccessful(UUID userId, String fullName, String email, String role) {
            events.add("OPEN:" + email + ":" + role);
            return UUID.randomUUID();
        }

        @Override
        public void recordFailure(String email) {
            events.add("FAIL:" + email);
        }

        @Override
        public void close(UUID sessionId, AccessSessionEndReason reason) {
            events.add("CLOSE:" + reason);
        }

        @Override
        public void revokeActiveByUser(UUID userId) {
            events.add("REVOKE:" + userId);
        }

        @Override
        public Optional<AccessSession> findById(UUID sessionId) {
            return Optional.empty();
        }

        @Override
        public Page<AccessSession> search(String email, String role, AccessSessionStatus status, String ipAddress,
                                           LocalDateTime from, LocalDateTime to, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public long countAll() {
            return 0;
        }

        @Override
        public long countByStatus(AccessSessionStatus status) {
            return 0;
        }

        @Override
        public long countStartedSince(LocalDateTime from) {
            return 0;
        }

        @Override
        public long countDistinctActiveUsers() {
            return 0;
        }
    }
}
