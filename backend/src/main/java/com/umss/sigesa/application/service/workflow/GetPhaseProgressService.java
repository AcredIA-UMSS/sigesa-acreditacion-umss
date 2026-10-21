package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.GetPhaseProgressUseCase;
import com.umss.sigesa.application.port.out.PhaseWorkflowPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.PhaseProgressResult;
import com.umss.sigesa.domain.model.PhaseState;
import com.umss.sigesa.domain.model.SubphaseState;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

public class GetPhaseProgressService implements GetPhaseProgressUseCase {

    private final PhaseWorkflowPort phaseWorkflowPort;

    public GetPhaseProgressService(PhaseWorkflowPort phaseWorkflowPort) {
        this.phaseWorkflowPort = phaseWorkflowPort;
    }

    @Override
    public PhaseProgressResult getProgress(UUID processId, UUID phaseId) {
        phaseWorkflowPort.findPhaseContext(processId, phaseId)
                .orElseThrow(() -> new ProcessNotFoundException(
                        "Fase no encontrada en el proceso: " + phaseId));

        List<PhaseWorkflowPort.SubphaseStatusItem> subphases =
                phaseWorkflowPort.listSubphasesWithStatus(phaseId);
        int totalSubphases = subphases.size();
        int approvedSubphases = (int) subphases.stream()
                .filter(subphase -> subphase.status() == SubphaseState.APROBADO)
                .count();
        double progressPercent = totalSubphases == 0
                ? 0.0
                : BigDecimal.valueOf(approvedSubphases)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalSubphases), 1, RoundingMode.HALF_UP)
                        .doubleValue();

        return new PhaseProgressResult(
                phaseId,
                totalSubphases,
                approvedSubphases,
                totalSubphases - approvedSubphases,
                progressPercent,
                phaseWorkflowPort.getCurrentState(phaseId));
    }
}
