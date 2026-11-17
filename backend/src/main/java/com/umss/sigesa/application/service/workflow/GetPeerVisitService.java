package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.GetPeerVisitUseCase;
import com.umss.sigesa.application.port.out.CoverageMatrixQueryPort;
import com.umss.sigesa.application.port.out.PeerVisitPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.evaluation.EvaluationAccessGuard;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;

import java.util.List;
import java.util.UUID;

public class GetPeerVisitService implements GetPeerVisitUseCase {

    private final ProcessQueryPort processQueryPort;
    private final PeerVisitPort peerVisitPort;
    private final CoverageMatrixQueryPort coverageMatrixQueryPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public GetPeerVisitService(
            ProcessQueryPort processQueryPort,
            PeerVisitPort peerVisitPort,
            CoverageMatrixQueryPort coverageMatrixQueryPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.processQueryPort = processQueryPort;
        this.peerVisitPort = peerVisitPort;
        this.coverageMatrixQueryPort = coverageMatrixQueryPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public PeerVisitView get(UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId, process.getCareerId(), requesterId, roles, jwtProgramScope, assignmentRepository);
        String careerName = coverageMatrixQueryPort.findCareerName(process.getCareerId()).orElse("Carrera");
        return new PeerVisitView(processId, careerName, peerVisitPort.findByProcessId(processId));
    }
}
