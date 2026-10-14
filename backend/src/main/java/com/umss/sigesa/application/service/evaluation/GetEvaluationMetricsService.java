package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.in.GetEvaluationMetricsUseCase;
import com.umss.sigesa.application.port.out.EvaluationMetricsPort;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.EvaluationMetrics;

import java.util.List;
import java.util.UUID;

public class GetEvaluationMetricsService implements GetEvaluationMetricsUseCase {

    private final ProcessQueryPort processQueryPort;
    private final EvaluationMetricsPort evaluationMetricsPort;
    private final MethodologicalStagePort stagePort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public GetEvaluationMetricsService(
            ProcessQueryPort processQueryPort,
            EvaluationMetricsPort evaluationMetricsPort,
            MethodologicalStagePort stagePort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.processQueryPort = processQueryPort;
        this.evaluationMetricsPort = evaluationMetricsPort;
        this.stagePort = stagePort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public EvaluationMetrics get(UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId,
                process.getCareerId(),
                requesterId,
                roles,
                jwtProgramScope,
                assignmentRepository);

        long total = evaluationMetricsPort.countIndicatorsByProcessId(processId);
        long withEvidence = evaluationMetricsPort.countIndicatorsWithEvidenceByProcessId(processId);
        long fodaComplete = evaluationMetricsPort.countFodaCompleteByProcessId(processId);
        long surveyBatches = stagePort.countPrimarySurveyBatches(processId);
        return EvaluationMetrics.of(processId, total, withEvidence, fodaComplete, surveyBatches);
    }
}
