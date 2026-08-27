package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.InvalidIndicatorStateException;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.IndicatorStateHistoryEntry;
import com.umss.sigesa.domain.model.IndicatorTransitionResult;
import com.umss.sigesa.domain.model.Role;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class IndicatorTransitionHelper {

    public static final Set<IndicatorState> REVIEWABLE_STATES =
            EnumSet.of(IndicatorState.SUBIDO, IndicatorState.SUBSANADO);

    private final IndicatorRepositoryPort indicatorRepository;

    public IndicatorTransitionHelper(IndicatorRepositoryPort indicatorRepository) {
        this.indicatorRepository = indicatorRepository;
    }

    public IndicatorTransitionResult transition(
            UUID indicatorId,
            IndicatorState targetState,
            UUID actorId,
            Role actorRole,
            Set<IndicatorState> allowedFrom) {
        indicatorRepository.findById(indicatorId)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));
        IndicatorState current = indicatorRepository.getCurrentState(indicatorId);
        if (!allowedFrom.contains(current)) {
            throw new InvalidIndicatorStateException(
                    "Indicador " + indicatorId + " en estado " + current
                            + "; se requiere " + allowedFrom);
        }
        UUID historyId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        indicatorRepository.appendStateHistory(new IndicatorStateHistoryEntry(
                historyId,
                indicatorId,
                current,
                targetState,
                actorId,
                actorRole,
                now));
        return new IndicatorTransitionResult(indicatorId, current, targetState, historyId);
    }
}
