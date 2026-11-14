package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.PhaseState;

import java.util.UUID;

/**
 * Transiciones de estado del workflow v2.0 sobre indicadores y cierre de Nivel 1.
 */
public interface NormativeIndicatorWorkflowPort {

    boolean hasBlockingEvidence(UUID indicatorId);

    boolean isWorkflowStarted(UUID indicatorId);

    void updateIndicatorStatus(UUID indicatorId, IndicatorState newStatus);

    void updateLevel1Status(UUID level1Id, PhaseState newStatus);
}
