package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.AppUser;

import java.util.List;
import java.util.UUID;

public interface ListCaeCandidatesUseCase {

    List<AppUser> list(UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope);
}
