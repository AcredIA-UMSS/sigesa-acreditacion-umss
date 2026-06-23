package com.umss.sigesa.adapter.out.auth;

import com.umss.sigesa.adapter.out.persistance.AppUserJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AppUserEntity;
import com.umss.sigesa.application.port.out.AuthPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.UserStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocalAuthAdapter implements AuthPort {

    private final AppUserJpaRepository userJpaRepository;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public LocalAuthAdapter(AppUserJpaRepository userJpaRepository,
                            UserProgramAssignmentRepositoryPort assignmentRepository,
                            PasswordEncoder passwordEncoder) {
        this.userJpaRepository = userJpaRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<AuthenticatedIdentity> authenticate(Email email, char[] rawPassword) {
        Optional<AppUserEntity> entityOpt = userJpaRepository.findByEmail(email.value());
        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }

        AppUserEntity entity = entityOpt.get();
        if (entity.getStatus() == UserStatus.DEACTIVATED) {
            return Optional.empty();
        }

        if (!PasswordUtils.matches(passwordEncoder, rawPassword, entity.getPasswordHash())) {
            return Optional.empty();
        }

        List<UUID> programScope = assignmentRepository.findActiveByUserId(entity.getId()).stream()
                .map(a -> a.getProgramId())
                .toList();

        return Optional.of(new AuthenticatedIdentity(
                entity.getId(),
                email,
                entity.getRole(),
                programScope
        ));
    }
}
