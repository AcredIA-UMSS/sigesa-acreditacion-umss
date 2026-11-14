package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ProcessFreeze;

import java.util.List;
import java.util.UUID;

public interface FreezeProcessUseCase {

    ProcessFreeze freeze(UUID processId, UUID actorId, List<String> roles, List<UUID> jwtProgramScope);
}
