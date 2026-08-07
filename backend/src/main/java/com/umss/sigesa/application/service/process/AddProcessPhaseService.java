package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.in.AddProcessPhaseUseCase;
import com.umss.sigesa.application.port.out.ProcessStructurePort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class AddProcessPhaseService implements AddProcessPhaseUseCase {

    private final ProcessStructurePort processStructurePort;
    private final ProcessStructureGuard guard;

    public AddProcessPhaseService(ProcessStructurePort processStructurePort, ProcessStructureGuard guard) {
        this.processStructurePort = processStructurePort;
        this.guard = guard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Phase execute(UUID processId, String name, Integer order, String description) {
        AccreditationProcess process = processStructurePort.loadActiveProcess(processId);
        guard.ensureProcessActive(process);
        guard.ensureUniquePhaseOrder(process, order, null);

        Phase phase = Phase.builder()
                .name(name)
                .order(order)
                .description(description)
                .build();

        return processStructurePort.savePhase(processId, phase);
    }
}
