package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.in.EnqueueDomainNotificationUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessResponsibleAssignment;
import com.umss.sigesa.domain.model.notification.NotificationEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveProcessResponsibleServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private ProcessResponsiblePort processResponsiblePort;
    @Mock
    private ProcessStructureGuard processStructureGuard;
    @Mock
    private EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase;

    private RemoveProcessResponsibleService service;

    @BeforeEach
    void setUp() {
        service = new RemoveProcessResponsibleService(
                processQueryPort,
                processResponsiblePort,
                processStructureGuard,
                enqueueDomainNotificationUseCase);
    }

    @Test
    void shouldRevokeAndNotifyRemovedCc() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID removedUserId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder()
                        .id(processId)
                        .careerId(careerId)
                        .status("ACTIVE")
                        .startDate(LocalDateTime.now())
                        .build()));
        when(processResponsiblePort.findActiveByProcessId(processId))
                .thenReturn(Optional.of(ProcessResponsibleAssignment.builder()
                        .processId(processId)
                        .userId(removedUserId)
                        .build()));

        service.remove(processId);

        verify(processResponsiblePort).revokeActiveByProcessId(processId);
        verify(enqueueDomainNotificationUseCase).enqueue(argThat(cmd ->
                cmd.eventType() == NotificationEventType.ProcessUnassigned
                        && processId.equals(cmd.processId())
                        && careerId.equals(cmd.programId())
                        && removedUserId.toString().equals(cmd.payloadValue("removedUserId"))));
    }

    @Test
    void shouldRevokeWithoutNotifyWhenNoActiveResponsible() {
        UUID processId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder()
                        .id(processId)
                        .careerId(UUID.randomUUID())
                        .status("ACTIVE")
                        .startDate(LocalDateTime.now())
                        .build()));
        when(processResponsiblePort.findActiveByProcessId(processId)).thenReturn(Optional.empty());

        service.remove(processId);

        verify(processResponsiblePort).revokeActiveByProcessId(processId);
        verify(enqueueDomainNotificationUseCase, never()).enqueue(org.mockito.ArgumentMatchers.any());
    }
}
