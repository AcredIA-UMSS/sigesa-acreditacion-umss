package com.umss.sigesa.application.service.auth.support;

import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.DuplicateEmailException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryUserRepository implements UserRepositoryPort {

    private final Map<UUID, StoredUser> usersById = new HashMap<>();

    @Override
    public AppUser save(AppUser user, char[] rawPassword) {
        if (findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateEmailException();
        }
        usersById.put(user.getId(), new StoredUser(user, new String(rawPassword)));
        return user;
    }

    @Override
    public Optional<AppUser> findByEmail(Email email) {
        return usersById.values().stream()
                .map(StoredUser::user)
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<AppUser> findById(UUID id) {
        StoredUser stored = usersById.get(id);
        return stored == null ? Optional.empty() : Optional.of(stored.user());
    }

    @Override
    public AppUser update(AppUser user) {
        StoredUser existing = usersById.get(user.getId());
        if (existing == null) {
            throw new IllegalStateException("Usuario no encontrado: " + user.getId());
        }
        usersById.put(user.getId(), new StoredUser(user, existing.password()));
        return user;
    }

    Optional<StoredUser> findStoredByEmail(Email email) {
        return usersById.values().stream()
                .filter(s -> s.user().getEmail().equals(email))
                .findFirst();
    }

    record StoredUser(AppUser user, String password) {
    }
}
