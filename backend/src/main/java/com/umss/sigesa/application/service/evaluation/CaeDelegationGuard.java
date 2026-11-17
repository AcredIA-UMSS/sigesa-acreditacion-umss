package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.out.IndicatorDelegationPort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.DelegationDeniedException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Role;

import java.util.Optional;
import java.util.UUID;

public class CaeDelegationGuard {

    private final UserRepositoryPort userRepository;
    private final IndicatorDelegationPort delegationPort;
    private final NormativeHierarchyQueryPort hierarchyQueryPort;

    public CaeDelegationGuard(
            UserRepositoryPort userRepository,
            IndicatorDelegationPort delegationPort,
            NormativeHierarchyQueryPort hierarchyQueryPort) {
        this.userRepository = userRepository;
        this.delegationPort = delegationPort;
        this.hierarchyQueryPort = hierarchyQueryPort;
    }

    public void assertCanWrite(UUID userId, UUID processId, UUID indicatorId) {
        Optional<AppUser> found = userRepository.findById(userId);
        if (found == null || found.isEmpty()) {
            return;
        }
        if (found.get().getRole() != Role.CAE_MEMBER) {
            return;
        }
        NormativeHierarchyQueryPort.IndicatorAncestry ancestry = hierarchyQueryPort
                .findIndicatorAncestry(indicatorId)
                .orElseThrow(DelegationDeniedException::new);
        boolean allowed = delegationPort.findByProcessIdAndUserId(processId, userId).stream()
                .anyMatch(delegation -> delegation.matches(
                        ancestry.indicatorId(), ancestry.level2Id(), ancestry.level1Id()));
        if (!allowed) {
            throw new DelegationDeniedException();
        }
    }
}
