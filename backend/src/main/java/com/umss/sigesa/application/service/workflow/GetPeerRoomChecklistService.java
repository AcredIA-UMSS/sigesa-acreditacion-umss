package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.GetPeerRoomChecklistUseCase;
import com.umss.sigesa.application.port.out.PeerRoomChecklistPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.evaluation.EvaluationAccessGuard;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.PeerRoomChecklistItem;

import java.util.List;
import java.util.UUID;

public class GetPeerRoomChecklistService implements GetPeerRoomChecklistUseCase {

    private final ProcessQueryPort processQueryPort;
    private final PeerRoomChecklistPort checklistPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public GetPeerRoomChecklistService(
            ProcessQueryPort processQueryPort,
            PeerRoomChecklistPort checklistPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.processQueryPort = processQueryPort;
        this.checklistPort = checklistPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<PeerRoomChecklistItem> get(
            UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId, process.getCareerId(), requesterId, roles, jwtProgramScope, assignmentRepository);
        return checklistPort.findByProcessId(processId);
    }
}
