package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ImportImprovementPlanResult;

import java.util.List;
import java.util.UUID;

public interface ImportImprovementPlanFromFodaUseCase {

    ImportImprovementPlanResult importFromFoda(
            UUID processId,
            UUID requesterId,
            List<String> roles,
            List<UUID> jwtProgramScope);
}
