package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.in.GetIndicatorAnalysisUseCase;
import com.umss.sigesa.application.port.out.IndicatorAnalysisPort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.IndicatorAnalysisNotFoundException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.model.IndicatorAnalysis;

import java.util.List;
import java.util.UUID;

public class GetIndicatorAnalysisService implements GetIndicatorAnalysisUseCase {

    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final IndicatorAnalysisPort analysisPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public GetIndicatorAnalysisService(
            NormativeHierarchyQueryPort hierarchyQueryPort,
            IndicatorAnalysisPort analysisPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.hierarchyQueryPort = hierarchyQueryPort;
        this.analysisPort = analysisPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public IndicatorAnalysis get(UUID indicatorId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope) {
        NormativeHierarchyQueryPort.NormativeIndicatorContext context = hierarchyQueryPort
                .findIndicatorContext(indicatorId)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));

        EvaluationAccessGuard.assertProcessVisible(
                context.processId(),
                context.careerId(),
                requesterId,
                roles,
                jwtProgramScope,
                assignmentRepository);

        return analysisPort.findByIndicatorId(indicatorId)
                .orElseThrow(() -> new IndicatorAnalysisNotFoundException(indicatorId));
    }
}
