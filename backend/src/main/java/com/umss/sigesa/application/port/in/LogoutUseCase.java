package com.umss.sigesa.application.port.in;

import java.util.UUID;

public interface LogoutUseCase {

    void logout(UUID userId);
}
