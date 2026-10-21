package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.PhaseProgress;
import com.umss.sigesa.application.port.in.GetPhaseProgressUseCase;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.PhaseState;

import java.util.List;
import java.util.UUID;

public class GetPhaseProgressService implements GetPhaseProgressUseCase {

    private final NormativeHierarchyQueryPort normativeHierarchyQueryPort;

    public GetPhaseProgressService(NormativeHierarchyQueryPort normativeHierarchyQueryPort) {
        this.normativeHierarchyQueryPort = normativeHierarchyQueryPort;
    }

    @Override
    public PhaseProgress getProgress(UUID processId, UUID phaseId) {
        NormativeHierarchyQueryPort.Level1Context phase = normativeHierarchyQueryPort
                .findLevel1Context(phaseId)
                .filter(context -> context.processId().equals(processId))
                .orElseThrow(() -> new ProcessNotFoundException(processId));

        List<NormativeHierarchyQueryPort.IndicatorStatusItem> indicators =
                normativeHierarchyQueryPort.listIndicatorsWithStatusByLevel1Id(phaseId);
        int total = indicators.size();
        int approved = (int) indicators.stream()
                .filter(indicator -> indicator.status() == IndicatorState.APROBADO)
                .count();
        double progressPercent = total == 0 ? 0.0 : Math.round(approved * 1000.0 / total) / 10.0;
        PhaseState state = phase.status();

        return new PhaseProgress(phaseId, total, approved, total - approved, progressPercent, state);
    }
}
