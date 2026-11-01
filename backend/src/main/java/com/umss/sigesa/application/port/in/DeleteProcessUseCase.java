package com.umss.sigesa.application.port.in;

import java.util.UUID;

public interface DeleteProcessUseCase {

    void delete(UUID processId);
}
