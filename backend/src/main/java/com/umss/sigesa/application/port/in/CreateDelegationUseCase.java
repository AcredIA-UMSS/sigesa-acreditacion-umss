package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.IndicatorDelegation;

import java.util.List;
import java.util.UUID;

public interface CreateDelegationUseCase {

    IndicatorDelegation create(
            UUID processId,
            UUID userId,
            UUID level1Id,
            UUID level2Id,
            UUID indicatorId,
            UUID requesterId,
            List<String> roles,
            List<UUID> jwtProgramScope);
}
