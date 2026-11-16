package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.PeerVisit;
import com.umss.sigesa.domain.model.PeerVisitItem;
import com.umss.sigesa.domain.model.PeerVisitStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface UpsertPeerVisitUseCase {

    record Command(
            LocalDate startsOn,
            LocalDate endsOn,
            String venue,
            String room,
            PeerVisitStatus status,
            String notes,
            List<PeerVisitItem> items
    ) {
    }

    PeerVisit upsert(
            UUID processId,
            Command command,
            UUID requesterId,
            List<String> roles,
            List<UUID> jwtProgramScope);
}
