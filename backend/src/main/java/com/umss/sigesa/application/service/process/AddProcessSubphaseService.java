package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.in.AddProcessSubphaseUseCase;
import com.umss.sigesa.application.port.out.ProcessStructurePort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class AddProcessSubphaseService implements AddProcessSubphaseUseCase {

    private final ProcessStructurePort processStructurePort;
    private final ProcessStructureGuard guard;

    public AddProcessSubphaseService(ProcessStructurePort processStructurePort, ProcessStructureGuard guard) {
        this.processStructurePort = processStructurePort;
        this.guard = guard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Subphase execute(UUID processId, UUID phaseId, String name, Integer order,
                            String referenceUrl, String description) {
        AccreditationProcess process = processStructurePort.loadActiveProcess(processId);
        guard.ensureProcessActive(process);
        Phase phase = guard.findPhase(process, phaseId);
        guard.ensureReferenceUrl(referenceUrl);
        guard.ensureUniqueSubphaseOrder(phase, order, null);

        Subphase subphase = Subphase.builder()
                .name(name)
                .order(order)
                .referenceUrl(referenceUrl.trim())
                .description(description)
                .build();

        return processStructurePort.saveSubphase(processId, phaseId, subphase);
    }
}
