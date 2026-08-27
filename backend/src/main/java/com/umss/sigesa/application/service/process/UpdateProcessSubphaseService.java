package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.in.UpdateProcessSubphaseUseCase;
import com.umss.sigesa.application.port.out.ProcessStructurePort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class UpdateProcessSubphaseService implements UpdateProcessSubphaseUseCase {

    private final ProcessStructurePort processStructurePort;
    private final ProcessStructureGuard guard;

    public UpdateProcessSubphaseService(ProcessStructurePort processStructurePort, ProcessStructureGuard guard) {
        this.processStructurePort = processStructurePort;
        this.guard = guard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Subphase execute(UUID processId, UUID phaseId, UUID subphaseId,
                             String name, Integer order, String referenceUrl, String description,
                             String requirements) {
        AccreditationProcess process = processStructurePort.loadActiveProcess(processId);
        guard.ensureProcessActive(process);
        Phase phase = guard.findPhase(process, phaseId);
        Subphase existing = guard.findSubphase(phase, subphaseId);

        String updatedName = name != null ? name : existing.getName();
        Integer updatedOrder = order != null ? order : existing.getOrder();
        String updatedReferenceUrl = referenceUrl != null ? referenceUrl : existing.getReferenceUrl();
        String updatedDescription = description != null ? description : existing.getDescription();
        String updatedRequirements = requirements != null ? requirements : existing.getRequirements();

        guard.ensureReferenceUrl(updatedReferenceUrl);
        guard.ensureRequirements(updatedRequirements);
        guard.ensureUniqueSubphaseOrder(phase, updatedOrder, subphaseId);

        Subphase updated = Subphase.builder()
                .id(existing.getId())
                .name(updatedName)
                .order(updatedOrder)
                .referenceUrl(updatedReferenceUrl.trim())
                .description(updatedDescription)
                .requirements(updatedRequirements.trim())
                .build();

        return processStructurePort.saveSubphase(processId, phaseId, updated);
    }
}
