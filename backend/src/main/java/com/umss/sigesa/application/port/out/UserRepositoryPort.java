package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    AppUser save(AppUser user, char[] rawPassword);

    Optional<AppUser> findByEmail(Email email);

    Optional<AppUser> findById(UUID id);

    AppUser update(AppUser user);
}
