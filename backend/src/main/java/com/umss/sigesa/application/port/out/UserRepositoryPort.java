package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    AppUser save(AppUser user, char[] rawPassword);

    Optional<AppUser> findByEmail(Email email);

    Optional<AppUser> findById(UUID id);

    AppUser update(AppUser user);

    List<AppUser> findAllFiltered(Role role, UserStatus status);
}
