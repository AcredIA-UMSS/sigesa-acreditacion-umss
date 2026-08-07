package com.umss.sigesa.application.port.in;

import java.util.UUID;

public interface RemoveProcessResponsibleUseCase {

    void remove(UUID processId);
}
