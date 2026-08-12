package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.AccreditationProcess;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReorderProcessStructureUseCase {

    AccreditationProcess execute(UUID processId, List<UUID> phaseIds,
                                 Map<UUID, List<UUID>> subphasesByPhase);
}
