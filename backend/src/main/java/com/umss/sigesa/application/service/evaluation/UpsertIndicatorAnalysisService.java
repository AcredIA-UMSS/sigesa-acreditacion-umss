package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.in.UpsertIndicatorAnalysisUseCase;
import com.umss.sigesa.application.port.out.IndicatorAnalysisPort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.ValidationException;
import com.umss.sigesa.domain.model.ComplianceJudgment;
import com.umss.sigesa.domain.model.FodaQuadrant;
import com.umss.sigesa.domain.model.IndicatorAnalysis;
import com.umss.sigesa.domain.model.UpsertIndicatorAnalysisCommand;

import java.time.LocalDateTime;
import java.util.UUID;

public class UpsertIndicatorAnalysisService implements UpsertIndicatorAnalysisUseCase {

    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final IndicatorAnalysisPort analysisPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public UpsertIndicatorAnalysisService(
            NormativeHierarchyQueryPort hierarchyQueryPort,
            IndicatorAnalysisPort analysisPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.hierarchyQueryPort = hierarchyQueryPort;
        this.analysisPort = analysisPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public IndicatorAnalysis upsert(UpsertIndicatorAnalysisCommand command) {
        if (command.indicatorId() == null) {
            throw new IndicatorNotFoundException(null);
        }
        NormativeHierarchyQueryPort.NormativeIndicatorContext context = hierarchyQueryPort
                .findIndicatorContext(command.indicatorId())
                .orElseThrow(() -> new IndicatorNotFoundException(command.indicatorId()));

        EvaluationAccessGuard.assertProcessVisible(
                context.processId(),
                context.careerId(),
                command.authorId(),
                command.roles(),
                command.jwtProgramScope(),
                assignmentRepository);

        FodaQuadrant quadrant = FodaQuadrant.from(command.fodaQuadrant())
                .orElseThrow(() -> new ValidationException("fodaQuadrant es obligatorio y debe ser STRENGTH, WEAKNESS, OPPORTUNITY o THREAT."));
        ComplianceJudgment compliance = ComplianceJudgment.from(command.compliance())
                .orElseThrow(() -> new ValidationException("compliance es obligatorio y debe ser COMPLIES, PARTIAL o DOES_NOT_COMPLY."));
        String synthesis = command.synthesis() == null ? "" : command.synthesis().trim();
        if (synthesis.isBlank()) {
            throw new ValidationException("La síntesis FODA no puede estar vacía.");
        }

        LocalDateTime now = LocalDateTime.now();
        IndicatorAnalysis analysis = analysisPort.findByIndicatorId(command.indicatorId())
                .orElseGet(() -> IndicatorAnalysis.builder()
                        .id(UUID.randomUUID())
                        .indicatorId(command.indicatorId())
                        .processId(context.processId())
                        .createdAt(now)
                        .build());
        analysis.apply(quadrant, compliance, synthesis, command.authorId(), now);
        return analysisPort.save(analysis);
    }
}
