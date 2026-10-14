package com.umss.sigesa.application.port.in;

import java.util.List;
import java.util.UUID;

public interface DeleteDelegationUseCase {

    void delete(UUID processId, UUID delegationId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope);
}
