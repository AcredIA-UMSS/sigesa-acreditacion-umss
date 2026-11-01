package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ImprovementPlanItem;

import java.util.List;
import java.util.UUID;

public interface ListImprovementPlanUseCase {

    List<ImprovementPlanItem> list(UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope);
}
