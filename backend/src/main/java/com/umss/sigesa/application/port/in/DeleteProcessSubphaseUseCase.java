package com.umss.sigesa.application.port.in;

import java.util.UUID;

public interface DeleteProcessSubphaseUseCase {

    void execute(UUID processId, UUID phaseId, UUID subphaseId);
}
