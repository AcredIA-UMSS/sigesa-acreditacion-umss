package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.Phase;

import java.util.UUID;

public interface AddProcessPhaseUseCase {

    Phase execute(UUID processId, String name, Integer order, String description);
}
