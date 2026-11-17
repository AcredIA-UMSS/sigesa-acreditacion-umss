package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.ProcessResponsibleInfo;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.ProcessResponsibleAssignment;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessResponsibleEnrichmentHelperTest {

    @Mock
    private ProcessResponsiblePort processResponsiblePort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private final UUID processA = UUID.randomUUID();
    private final UUID processB = UUID.randomUUID();
    private final UUID userA = UUID.randomUUID();
    private final UUID userB = UUID.randomUUID();
    private final LocalDateTime assignedAt = LocalDateTime.now();

    @Test
    void resolveForProcessReturnsInfoWhenFound() {
        ProcessResponsibleAssignment assignment = ProcessResponsibleAssignment.builder()
                .processId(processA)
                .userId(userA)
                .assignedAt(assignedAt)
                .build();
        AppUser user = new AppUser(
                userA,
                Email.of("usera@umss.edu.bo"),
                Role.CC,
                UserStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Carlos",
                "Sanchez",
                null
        );

        when(processResponsiblePort.findActiveByProcessId(processA)).thenReturn(Optional.of(assignment));
        when(userRepositoryPort.findById(userA)).thenReturn(Optional.of(user));

        Optional<ProcessResponsibleInfo> result =
                ProcessResponsibleEnrichmentHelper.resolveForProcess(processA, processResponsiblePort, userRepositoryPort);

        assertTrue(result.isPresent());
        assertEquals("Carlos Sanchez", result.get().fullName());
        assertEquals("usera@umss.edu.bo", result.get().email());
    }

    @Test
    void resolveForProcessReturnsEmptyWhenNoAssignment() {
        when(processResponsiblePort.findActiveByProcessId(processA)).thenReturn(Optional.empty());

        Optional<ProcessResponsibleInfo> result =
                ProcessResponsibleEnrichmentHelper.resolveForProcess(processA, processResponsiblePort, userRepositoryPort);

        assertFalse(result.isPresent());
        verifyNoInteractions(userRepositoryPort);
    }

    @Test
    void resolveForProcessesReturnsEmptyForNullOrEmptyList() {
        assertTrue(ProcessResponsibleEnrichmentHelper.resolveForProcesses(null, processResponsiblePort, userRepositoryPort).isEmpty());
        assertTrue(ProcessResponsibleEnrichmentHelper.resolveForProcesses(List.of(), processResponsiblePort, userRepositoryPort).isEmpty());
        verifyNoInteractions(processResponsiblePort, userRepositoryPort);
    }

    @Test
    void resolveForProcessesResolvesInBatch() {
        ProcessResponsibleAssignment assignA = ProcessResponsibleAssignment.builder()
                .processId(processA)
                .userId(userA)
                .assignedAt(assignedAt)
                .build();
        ProcessResponsibleAssignment assignB = ProcessResponsibleAssignment.builder()
                .processId(processB)
                .userId(userB)
                .assignedAt(assignedAt)
                .build();

        AppUser userModelA = new AppUser(
                userA,
                Email.of("usera@umss.edu.bo"),
                Role.CC,
                UserStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Carlos",
                "Sanchez",
                null
        );
        AppUser userModelB = new AppUser(
                userB,
                Email.of("userb@umss.edu.bo"),
                Role.CC,
                UserStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "Maria",
                "Gomez",
                null
        );

        when(processResponsiblePort.findActiveByProcessIds(List.of(processA, processB)))
                .thenReturn(List.of(assignA, assignB));
        when(userRepositoryPort.findByIds(List.of(userA, userB)))
                .thenReturn(Map.of(userA, userModelA, userB, userModelB));

        Map<UUID, ProcessResponsibleInfo> result = ProcessResponsibleEnrichmentHelper.resolveForProcesses(
                List.of(processA, processB), processResponsiblePort, userRepositoryPort);

        assertEquals(2, result.size());
        assertEquals("Carlos Sanchez", result.get(processA).fullName());
        assertEquals("Maria Gomez", result.get(processB).fullName());
        verify(processResponsiblePort).findActiveByProcessIds(List.of(processA, processB));
        verify(userRepositoryPort).findByIds(List.of(userA, userB));
    }
}
