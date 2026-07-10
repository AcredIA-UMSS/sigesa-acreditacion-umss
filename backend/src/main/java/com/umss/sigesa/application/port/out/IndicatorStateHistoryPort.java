package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.Role;
import java.util.Optional;
import java.util.UUID;

public interface IndicatorStateHistoryPort {
    void recordTransition(UUID indicatorId, String previousState, String newState, UUID actorId, Role role);

    Optional<String> findLatestState(UUID indicatorId);

    Optional<UUID> findLatestHistoryId(UUID indicatorId);
}
