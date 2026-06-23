package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;

import java.util.UUID;

public interface RegisterUserUseCase {

    RegisterResult register(String email, Role role, UUID programId, char[] temporaryPassword);

    record RegisterResult(UUID userId, UserStatus status) {
    }
}
