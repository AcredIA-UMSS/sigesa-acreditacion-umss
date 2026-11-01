package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.in.ImportImprovementPlanFromFodaUseCase;
import com.umss.sigesa.application.port.out.ImprovementPlanItemPort;
import com.umss.sigesa.application.port.out.IndicatorAnalysisPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.workflow.ProcessFreezeGuard;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.FodaQuadrant;
import com.umss.sigesa.domain.model.ImportImprovementPlanResult;
import com.umss.sigesa.domain.model.ImprovementPlanItem;
import com.umss.sigesa.domain.model.IndicatorAnalysis;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ImportImprovementPlanFromFodaService implements ImportImprovementPlanFromFodaUseCase {

    private final ProcessQueryPort processQueryPort;
    private final ImprovementPlanItemPort planItemPort;
    private final IndicatorAnalysisPort analysisPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final ProcessFreezeGuard freezeGuard;

    public ImportImprovementPlanFromFodaService(
            ProcessQueryPort processQueryPort,
            ImprovementPlanItemPort planItemPort,
            IndicatorAnalysisPort analysisPort,
            UserProgramAssignmentRepositoryPort assignmentRepository,
            ProcessFreezeGuard freezeGuard) {
        this.processQueryPort = processQueryPort;
        this.planItemPort = planItemPort;
        this.analysisPort = analysisPort;
        this.assignmentRepository = assignmentRepository;
        this.freezeGuard = freezeGuard;
    }

    @Override
    public ImportImprovementPlanResult importFromFoda(
            UUID processId,
            UUID requesterId,
            List<String> roles,
            List<UUID> jwtProgramScope) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId,
                process.getCareerId(),
                requesterId,
                roles,
                jwtProgramScope,
                assignmentRepository);
        freezeGuard.assertWritable(processId);

        List<IndicatorAnalysis> weaknesses = analysisPort.findByProcessId(processId).stream()
                .filter(analysis -> analysis.getFodaQuadrant() == FodaQuadrant.WEAKNESS)
                .toList();

        int imported = 0;
        int skipped = 0;
        LocalDateTime now = LocalDateTime.now();
        for (IndicatorAnalysis analysis : weaknesses) {
            if (analysis.getIndicatorId() == null
                    || planItemPort.existsByProcessIdAndSourceIndicatorId(processId, analysis.getIndicatorId())) {
                skipped++;
                continue;
            }
            planItemPort.save(ImprovementPlanItem.createFromWeakness(
                    processId,
                    analysis.getIndicatorId(),
                    analysis.getSynthesis(),
                    now));
            imported++;
        }

        return new ImportImprovementPlanResult(imported, skipped, planItemPort.findByProcessId(processId));
    }
}
