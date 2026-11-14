package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.IndicatorDelegation;

import java.util.List;
import java.util.UUID;

public interface ListDelegationsUseCase {

    List<IndicatorDelegation> list(UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope);
}
