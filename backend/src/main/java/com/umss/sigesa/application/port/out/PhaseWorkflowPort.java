package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.PhaseState;
import com.umss.sigesa.domain.model.SubphaseState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhaseWorkflowPort {

    Optional<PhaseContext> findPhaseContext(UUID processId, UUID phaseId);

    PhaseState getCurrentState(UUID phaseId);

    void updateState(UUID phaseId, PhaseState newState);

    List<SubphaseStatusItem> listSubphasesWithStatus(UUID phaseId);

    record PhaseContext(UUID phaseId, UUID processId, UUID careerId, String phaseName) {
    }

    record SubphaseStatusItem(UUID subphaseId, String name, SubphaseState status, Integer order) {
    }
}
