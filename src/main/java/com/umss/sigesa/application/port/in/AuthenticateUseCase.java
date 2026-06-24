package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.port.out.IssuedToken;
import com.umss.sigesa.domain.model.Role;

import java.util.List;
import java.util.UUID;

public interface AuthenticateUseCase {

    LoginResult authenticate(String email, String password);

    record LoginResult(IssuedToken token, Role role, List<UUID> programScope) {
    }
}
