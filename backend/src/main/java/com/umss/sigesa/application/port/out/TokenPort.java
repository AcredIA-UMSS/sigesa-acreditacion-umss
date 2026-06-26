package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.AuthenticatedIdentity;

public interface TokenPort {

    IssuedToken issue(AuthenticatedIdentity identity);
}
