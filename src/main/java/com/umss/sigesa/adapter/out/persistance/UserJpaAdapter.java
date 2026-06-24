package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.auth.PasswordUtils;
import com.umss.sigesa.adapter.out.persistance.entity.AppUserEntity;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.DuplicateEmailException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserJpaAdapter implements UserRepositoryPort {

    private final AppUserJpaRepository jpaRepository;
    private final PasswordEncoder passwordEncoder;

    public UserJpaAdapter(AppUserJpaRepository jpaRepository, PasswordEncoder passwordEncoder) {
        this.jpaRepository = jpaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AppUser save(AppUser user, char[] rawPassword) {
        AppUserEntity entity = toEntity(user);
        entity.setPasswordHash(PasswordUtils.encode(passwordEncoder, rawPassword));
        try {
            AppUserEntity saved = jpaRepository.save(entity);
            return toDomain(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEmailException();
        }
    }

    @Override
    public Optional<AppUser> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.value()).map(this::toDomain);
    }

    @Override
    public Optional<AppUser> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public AppUser update(AppUser user) {
        AppUserEntity entity = jpaRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado para actualizar."));
        entity.setStatus(user.getStatus());
        entity.setUpdatedAt(user.getUpdatedAt());
        return toDomain(jpaRepository.save(entity));
    }

    private AppUserEntity toEntity(AppUser user) {
        AppUserEntity entity = new AppUserEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail().value());
        entity.setRole(user.getRole());
        entity.setStatus(user.getStatus());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }

    private AppUser toDomain(AppUserEntity entity) {
        return new AppUser(
                entity.getId(),
                Email.of(entity.getEmail()),
                entity.getRole(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
