package com.umss.sigesa.application.service.subphase;

import com.umss.sigesa.application.port.in.ListSubphaseObservationsUseCase;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.application.port.out.SubphaseQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.model.SubphaseObservation;

import java.util.List;
import java.util.UUID;

public class ListSubphaseObservationsService implements ListSubphaseObservationsUseCase {

    private final SubphaseQueryPort subphaseQueryPort;
    private final SubphaseObservationPort observationPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public ListSubphaseObservationsService(SubphaseQueryPort subphaseQueryPort,
                                           SubphaseObservationPort observationPort,
                                           UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.subphaseQueryPort = subphaseQueryPort;
        this.observationPort = observationPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<SubphaseObservation> list(UUID subphaseId, UUID requesterId, List<String> roles) {
        SubphaseQueryPort.SubphaseContext context = subphaseQueryPort.findContext(subphaseId)
                .orElseThrow(() -> new ProcessNotFoundException("Subfase no encontrada: " + subphaseId));
        assertAccess(context.careerId(), requesterId, roles);
        return observationPort.findBySubphaseId(subphaseId);
    }

    private void assertAccess(UUID careerId, UUID requesterId, List<String> roles) {
        if (roles != null && roles.stream().anyMatch(r -> "JD".equals(r) || "TD".equals(r))) {
            return;
        }
        boolean allowed = assignmentRepository.findActiveByUserId(requesterId).stream()
                .anyMatch(a -> a.getProgramId().equals(careerId));
        if (!allowed) {
            throw new ProgramScopeDeniedException();
        }
    }
}
