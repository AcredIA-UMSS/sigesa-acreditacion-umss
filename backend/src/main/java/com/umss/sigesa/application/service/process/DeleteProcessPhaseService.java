package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.in.DeleteProcessPhaseUseCase;
import com.umss.sigesa.application.port.out.ProcessStructurePort;
import com.umss.sigesa.application.port.out.SubphaseWorkflowPort;
import com.umss.sigesa.domain.exception.SubphaseHasEvidenceException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteProcessPhaseService implements DeleteProcessPhaseUseCase {

    private final ProcessStructurePort processStructurePort;
    private final SubphaseWorkflowPort subphaseWorkflowPort;
    private final ProcessStructureGuard guard;

    public DeleteProcessPhaseService(ProcessStructurePort processStructurePort,
                                     SubphaseWorkflowPort subphaseWorkflowPort,
                                     ProcessStructureGuard guard) {
        this.processStructurePort = processStructurePort;
        this.subphaseWorkflowPort = subphaseWorkflowPort;
        this.guard = guard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(UUID processId, UUID phaseId) {
        AccreditationProcess process = processStructurePort.loadActiveProcess(processId);
        guard.ensureProcessActive(process);
        Phase phase = guard.findPhase(process, phaseId);

        for (Subphase subphase : phase.getSubphases()) {
            if (subphaseWorkflowPort.hasBlockingEvidence(subphase.getId())) {
                throw new SubphaseHasEvidenceException(subphase.getId());
            }
        }

        processStructurePort.deletePhase(processId, phaseId);
    }
}
