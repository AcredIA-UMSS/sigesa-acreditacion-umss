package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.UserStatus;

import java.util.UUID;

public interface RegisterUserUseCase {

    RegisterResult register(RegisterUserCommand command);

    record RegisterUserCommand(
            String email,
            String roleName,
            UUID programId,
            String firstName,
            String lastName,
            String phoneNumber,
            char[] password
    ) {
    }

    record RegisterResult(UUID userId, UserStatus status) {
    }
}
