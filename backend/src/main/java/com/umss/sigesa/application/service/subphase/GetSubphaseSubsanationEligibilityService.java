package com.umss.sigesa.application.service.subphase;

import com.umss.sigesa.application.port.in.GetSubphaseSubsanationEligibilityUseCase;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.application.port.out.SubphaseQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.SubphaseObservation;
import com.umss.sigesa.domain.model.SubphaseSubsanationEligibility;

import java.util.List;
import java.util.UUID;

public class GetSubphaseSubsanationEligibilityService implements GetSubphaseSubsanationEligibilityUseCase {

    private final SubphaseQueryPort subphaseQueryPort;
    private final SubphaseObservationPort observationPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public GetSubphaseSubsanationEligibilityService(SubphaseQueryPort subphaseQueryPort,
                                                    SubphaseObservationPort observationPort,
                                                    UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.subphaseQueryPort = subphaseQueryPort;
        this.observationPort = observationPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public SubphaseSubsanationEligibility get(UUID subphaseId, UUID userId, List<String> roles) {
        SubphaseQueryPort.SubphaseContext context = subphaseQueryPort.findContext(subphaseId)
                .orElseThrow(() -> new ProcessNotFoundException("Subfase no encontrada: " + subphaseId));

        if (!roles.contains(Role.CC.name())) {
            return new SubphaseSubsanationEligibility(false, null,
                    "Solo el coordinador [CC] puede subsanar evidencias.");
        }

        if (!isCcInScope(userId, context.careerId())) {
            return new SubphaseSubsanationEligibility(false, null,
                    "La subfase no pertenece a su carrera asignada.");
        }

        SubphaseObservation open = observationPort.findLatestOpenBySubphaseId(subphaseId).orElse(null);
        if (open == null) {
            return new SubphaseSubsanationEligibility(false, null,
                    "No hay observación pendiente del equipo técnico.");
        }

        return new SubphaseSubsanationEligibility(true, open.getId(),
                "Puede subsanar una vez por observación pendiente.");
    }

    private boolean isCcInScope(UUID userId, UUID programId) {
        return assignmentRepository.findActiveByUserId(userId).stream()
                .anyMatch(a -> a.getProgramId().equals(programId));
    }
}
