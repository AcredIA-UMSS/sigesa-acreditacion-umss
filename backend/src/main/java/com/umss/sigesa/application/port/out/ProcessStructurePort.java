package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;

import java.util.List;
import java.util.UUID;

public interface ProcessStructurePort {

    AccreditationProcess loadActiveProcess(UUID processId);

    Phase savePhase(UUID processId, Phase phase);

    Subphase saveSubphase(UUID processId, UUID phaseId, Subphase subphase);

    void deletePhase(UUID processId, UUID phaseId);

    void deleteSubphase(UUID processId, UUID phaseId, UUID subphaseId);

    void reorderPhases(UUID processId, List<UUID> phaseIdsInOrder);

    void reorderSubphases(UUID processId, UUID phaseId, List<UUID> subphaseIdsInOrder);
}
