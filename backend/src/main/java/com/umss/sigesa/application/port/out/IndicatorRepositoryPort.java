package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.Indicator;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.IndicatorStateHistoryEntry;

import java.util.Optional;
import java.util.UUID;

public interface IndicatorRepositoryPort {

    Optional<Indicator> findById(UUID id);

    IndicatorState getCurrentState(UUID indicatorId);

    void appendStateHistory(IndicatorStateHistoryEntry entry);
}
