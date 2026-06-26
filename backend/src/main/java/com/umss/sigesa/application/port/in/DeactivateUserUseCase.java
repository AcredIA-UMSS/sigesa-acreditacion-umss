package com.umss.sigesa.application.port.in;

import java.util.UUID;

public interface DeactivateUserUseCase {

    void deactivate(UUID userId);
}
