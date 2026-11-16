package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.PeerVisit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetPeerVisitUseCase {

    record PeerVisitView(UUID processId, String careerName, Optional<PeerVisit> visit) {
    }

    PeerVisitView get(UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope);
}
