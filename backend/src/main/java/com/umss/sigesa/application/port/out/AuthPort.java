package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.Email;

import java.util.Optional;

public interface AuthPort {

    Optional<AuthenticatedIdentity> authenticate(Email email, char[] rawPassword);
}
