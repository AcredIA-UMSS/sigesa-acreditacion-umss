package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.in.ReorderProcessStructureUseCase;
import com.umss.sigesa.application.port.out.ProcessStructurePort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReorderProcessStructureService implements ReorderProcessStructureUseCase {

    private final ProcessStructurePort processStructurePort;
    private final ProcessStructureGuard guard;

    public ReorderProcessStructureService(ProcessStructurePort processStructurePort, ProcessStructureGuard guard) {
        this.processStructurePort = processStructurePort;
        this.guard = guard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccreditationProcess execute(UUID processId, List<UUID> phaseIds,
                                          Map<UUID, List<UUID>> subphasesByPhase) {
        AccreditationProcess process = processStructurePort.loadActiveProcess(processId);
        guard.ensureProcessActive(process);

        if (phaseIds != null && !phaseIds.isEmpty()) {
            processStructurePort.reorderPhases(processId, phaseIds);
        }

        if (subphasesByPhase != null) {
            for (Map.Entry<UUID, List<UUID>> entry : subphasesByPhase.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    processStructurePort.reorderSubphases(processId, entry.getKey(), entry.getValue());
                }
            }
        }

        return processStructurePort.loadActiveProcess(processId);
    }
}
