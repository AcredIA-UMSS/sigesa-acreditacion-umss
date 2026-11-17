package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.out.IndicatorDelegationPort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.DelegationDeniedException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.IndicatorDelegation;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaeDelegationGuardTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private IndicatorDelegationPort delegationPort;
    @Mock
    private NormativeHierarchyQueryPort hierarchyQueryPort;

    private CaeDelegationGuard guard;

    @BeforeEach
    void setUp() {
        guard = new CaeDelegationGuard(userRepository, delegationPort, hierarchyQueryPort);
    }

    @Test
    void ccUserIsSkipped() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, Role.CC)));
        guard.assertCanWrite(userId, UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    void caeWithoutDelegationIsDenied() {
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, Role.CAE_MEMBER)));
        when(hierarchyQueryPort.findIndicatorAncestry(indicatorId)).thenReturn(Optional.of(
                new NormativeHierarchyQueryPort.IndicatorAncestry(
                        indicatorId, processId, UUID.randomUUID(), UUID.randomUUID())));
        when(delegationPort.findByProcessIdAndUserId(processId, userId)).thenReturn(List.of());

        assertThrows(DelegationDeniedException.class,
                () -> guard.assertCanWrite(userId, processId, indicatorId));
    }

    @Test
    void caeWithExactIndicatorIsAllowed() {
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();
        UUID level2Id = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, Role.CAE_MEMBER)));
        when(hierarchyQueryPort.findIndicatorAncestry(indicatorId)).thenReturn(Optional.of(
                new NormativeHierarchyQueryPort.IndicatorAncestry(indicatorId, processId, level1Id, level2Id)));
        when(delegationPort.findByProcessIdAndUserId(processId, userId)).thenReturn(List.of(
                IndicatorDelegation.create(processId, userId, null, null, indicatorId, LocalDateTime.now())));

        guard.assertCanWrite(userId, processId, indicatorId);
    }

    @Test
    void caeWithLevel1AncestorIsAllowed() {
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        UUID indicatorId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();
        UUID level2Id = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, Role.CAE_MEMBER)));
        when(hierarchyQueryPort.findIndicatorAncestry(indicatorId)).thenReturn(Optional.of(
                new NormativeHierarchyQueryPort.IndicatorAncestry(indicatorId, processId, level1Id, level2Id)));
        when(delegationPort.findByProcessIdAndUserId(processId, userId)).thenReturn(List.of(
                IndicatorDelegation.create(processId, userId, level1Id, null, null, LocalDateTime.now())));

        guard.assertCanWrite(userId, processId, indicatorId);
    }

    private static AppUser user(UUID userId, Role role) {
        return new AppUser(
                userId, Email.of("user@umss.edu.bo"), role, UserStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), "Ana", "Cae", "70123456");
    }
}
