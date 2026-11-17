package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.PeerVisit;

import java.util.Optional;
import java.util.UUID;

public interface PeerVisitPort {

    Optional<PeerVisit> findByProcessId(UUID processId);

    PeerVisit save(PeerVisit visit);
}
