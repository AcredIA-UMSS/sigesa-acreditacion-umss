package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.GetNormativeIndicatorSubsanationEligibilityUseCase;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.model.IndicatorObservation;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.NormativeIndicatorSubsanationEligibility;

import java.util.List;
import java.util.UUID;

public class GetNormativeIndicatorSubsanationEligibilityService
        implements GetNormativeIndicatorSubsanationEligibilityUseCase {

    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final NormativeIndicatorObservationPort observationPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public GetNormativeIndicatorSubsanationEligibilityService(
            NormativeHierarchyQueryPort hierarchyQueryPort,
            NormativeIndicatorObservationPort observationPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.hierarchyQueryPort = hierarchyQueryPort;
        this.observationPort = observationPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public NormativeIndicatorSubsanationEligibility get(UUID indicatorId, UUID userId, List<String> roles) {
        NormativeHierarchyQueryPort.NormativeIndicatorContext context = hierarchyQueryPort
                .findIndicatorContext(indicatorId)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));

        if (!roles.contains(Role.CC.name())) {
            return new NormativeIndicatorSubsanationEligibility(false, null,
                    "Solo el coordinador [CC] puede subsanar evidencias.");
        }

        if (!isCcInScope(userId, context.careerId())) {
            return new NormativeIndicatorSubsanationEligibility(false, null,
                    "El indicador no pertenece a su carrera asignada.");
        }

        IndicatorObservation open = observationPort.findLatestOpenByIndicatorId(indicatorId).orElse(null);
        if (open == null) {
            return new NormativeIndicatorSubsanationEligibility(false, null,
                    "No hay observación pendiente del equipo técnico.");
        }

        return new NormativeIndicatorSubsanationEligibility(true, open.getId(),
                "Puede subsanar una vez por observación pendiente.");
    }

    private boolean isCcInScope(UUID userId, UUID programId) {
        return assignmentRepository.findActiveByUserId(userId).stream()
                .anyMatch(a -> a.getProgramId().equals(programId));
    }
}
