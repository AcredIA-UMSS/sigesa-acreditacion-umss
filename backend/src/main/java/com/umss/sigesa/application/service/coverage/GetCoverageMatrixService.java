package com.umss.sigesa.application.service.coverage;

import com.umss.sigesa.application.port.in.GetCoverageMatrixUseCase;
import com.umss.sigesa.application.port.out.CoverageMatrixPdfPort;
import com.umss.sigesa.application.port.out.CoverageMatrixQueryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.evaluation.EvaluationAccessGuard;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.CoverageMatrix;
import com.umss.sigesa.domain.model.CoverageMatrixAssembler;

import java.util.List;
import java.util.UUID;

public class GetCoverageMatrixService implements GetCoverageMatrixUseCase {

    private final ProcessQueryPort processQueryPort;
    private final CoverageMatrixQueryPort queryPort;
    private final CoverageMatrixPdfPort pdfPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public GetCoverageMatrixService(
            ProcessQueryPort processQueryPort,
            CoverageMatrixQueryPort queryPort,
            CoverageMatrixPdfPort pdfPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.processQueryPort = processQueryPort;
        this.queryPort = queryPort;
        this.pdfPort = pdfPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public CoverageMatrix get(UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope) {
        AccreditationProcess process = requireVisible(processId, requesterId, roles, jwtProgramScope);
        String careerName = queryPort.findCareerName(process.getCareerId()).orElse("Carrera");
        return CoverageMatrixAssembler.assemble(processId, careerName, queryPort.findIndicatorRows(processId));
    }

    @Override
    public byte[] renderPdf(UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope) {
        return pdfPort.render(get(processId, requesterId, roles, jwtProgramScope));
    }

    private AccreditationProcess requireVisible(
            UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId, process.getCareerId(), requesterId, roles, jwtProgramScope, assignmentRepository);
        return process;
    }
}
