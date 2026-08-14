package com.umss.sigesa.application.port.out;

import com.umss.sigesa.application.model.evidence.EvidenceControlItem;
import com.umss.sigesa.application.model.evidence.UploadableIndicator;
import com.umss.sigesa.domain.model.IndicatorState;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface EvidenceControlQueryPort {

    /**
     * Lists indicators (with optional evidence metadata) filtered by program scope and current state.
     * Empty {@code programIds} means all programs (JD/TD institutional scope).
     */
    List<EvidenceControlItem> listByProgramIdsAndStates(List<UUID> programIds, Set<IndicatorState> states);

    Optional<EvidenceControlItem> findByIndicatorId(UUID indicatorId);

    /**
     * Indicadores cargables (UC-004) con etiquetas de presentación, filtrados por carrera y estado.
     */
    List<UploadableIndicator> listUploadableByProgramIds(List<UUID> programIds, Set<IndicatorState> states);
}
