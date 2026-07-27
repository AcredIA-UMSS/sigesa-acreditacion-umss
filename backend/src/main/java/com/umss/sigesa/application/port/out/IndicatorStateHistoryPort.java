package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Role;

import java.util.UUID;

/**
 * Retained for merge compatibility. Prefer {@link IndicatorRepositoryPort#appendStateHistory}
 * / EvidenceUploadJpaAdapter for MOD-EVIDENCE (Marlen).
 */
public interface IndicatorStateHistoryPort {
    void recordTransition(UUID indicatorId, IndicatorState previousState, IndicatorState newState,
                          UUID actorId, Role role);
}
