package com.umss.sigesa.application.service.auth.support;

import com.umss.sigesa.application.port.out.AuthPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InMemoryAuthPort implements AuthPort {

    private final InMemoryUserRepository userRepository;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public InMemoryAuthPort(InMemoryUserRepository userRepository,
                            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public Optional<AuthenticatedIdentity> authenticate(Email email, char[] rawPassword) {
        Optional<InMemoryUserRepository.StoredUser> storedOpt = userRepository.findStoredByEmail(email);
        if (storedOpt.isEmpty()) {
            return Optional.empty();
        }

        InMemoryUserRepository.StoredUser stored = storedOpt.get();
        if (stored.user().getStatus() == UserStatus.DEACTIVATED) {
            return Optional.empty();
        }
        if (!stored.password().equals(new String(rawPassword))) {
            return Optional.empty();
        }

        UUID userId = stored.user().getId();
        List<UUID> programScope = assignmentRepository.findActiveByUserId(userId).stream()
                .map(a -> a.getProgramId())
                .toList();

        return Optional.of(new AuthenticatedIdentity(
                userId,
                email,
                stored.user().getRole(),
                programScope
        ));
    }
}
