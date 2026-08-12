package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.in.DeleteProcessSubphaseUseCase;
import com.umss.sigesa.application.port.out.ProcessStructurePort;
import com.umss.sigesa.application.port.out.SubphaseWorkflowPort;
import com.umss.sigesa.domain.exception.SubphaseHasEvidenceException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteProcessSubphaseService implements DeleteProcessSubphaseUseCase {

    private final ProcessStructurePort processStructurePort;
    private final SubphaseWorkflowPort subphaseWorkflowPort;
    private final ProcessStructureGuard guard;

    public DeleteProcessSubphaseService(ProcessStructurePort processStructurePort,
                                        SubphaseWorkflowPort subphaseWorkflowPort,
                                        ProcessStructureGuard guard) {
        this.processStructurePort = processStructurePort;
        this.subphaseWorkflowPort = subphaseWorkflowPort;
        this.guard = guard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(UUID processId, UUID phaseId, UUID subphaseId) {
        AccreditationProcess process = processStructurePort.loadActiveProcess(processId);
        guard.ensureProcessActive(process);
        Phase phase = guard.findPhase(process, phaseId);
        guard.findSubphase(phase, subphaseId);

        if (subphaseWorkflowPort.hasBlockingEvidence(subphaseId)) {
            throw new SubphaseHasEvidenceException(subphaseId);
        }

        processStructurePort.deleteSubphase(processId, phaseId, subphaseId);
    }
}
