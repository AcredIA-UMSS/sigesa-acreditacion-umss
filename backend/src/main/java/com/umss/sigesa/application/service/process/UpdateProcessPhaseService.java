package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.in.UpdateProcessPhaseUseCase;
import com.umss.sigesa.application.port.out.ProcessStructurePort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class UpdateProcessPhaseService implements UpdateProcessPhaseUseCase {

    private final ProcessStructurePort processStructurePort;
    private final ProcessStructureGuard guard;

    public UpdateProcessPhaseService(ProcessStructurePort processStructurePort, ProcessStructureGuard guard) {
        this.processStructurePort = processStructurePort;
        this.guard = guard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Phase execute(UUID processId, UUID phaseId, String name, Integer order, String description) {
        AccreditationProcess process = processStructurePort.loadActiveProcess(processId);
        guard.ensureProcessActive(process);
        Phase existing = guard.findPhase(process, phaseId);

        String updatedName = name != null ? name : existing.getName();
        Integer updatedOrder = order != null ? order : existing.getOrder();
        String updatedDescription = description != null ? description : existing.getDescription();

        guard.ensureUniquePhaseOrder(process, updatedOrder, phaseId);

        Phase updated = Phase.builder()
                .id(existing.getId())
                .name(updatedName)
                .order(updatedOrder)
                .description(updatedDescription)
                .subphases(existing.getSubphases())
                .build();

        return processStructurePort.savePhase(processId, updated);
    }
}
