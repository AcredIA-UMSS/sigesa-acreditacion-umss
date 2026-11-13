package com.umss.sigesa.application.service.notification;

import com.umss.sigesa.application.port.in.EnqueueDomainNotificationUseCase;
import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.notification.DomainNotificationCommand;
import com.umss.sigesa.domain.model.notification.NotificationEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseAccreditationProcessServiceTest {

    @Mock
    private AccreditationProcessPort processPort;
    @Mock
    private EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase;

    @InjectMocks
    private CloseAccreditationProcessService service;

    @Test
    @DisplayName("Closes process then enqueues ProcessClosed without EE gate")
    void closesAndEnqueues() {
        UUID processId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId)
                .careerId(careerId)
                .status(ProcessStatus.ACTIVE.name())
                .build();
        when(processPort.findById(processId)).thenReturn(Optional.of(process));

        service.close(processId, UUID.randomUUID(), "JD");

        assertThat(process.getStatus()).isEqualTo(ProcessStatus.CLOSED.name());
        verify(processPort).save(process);

        ArgumentCaptor<DomainNotificationCommand> captor = ArgumentCaptor.forClass(DomainNotificationCommand.class);
        verify(enqueueDomainNotificationUseCase).enqueue(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(NotificationEventType.ProcessClosed);
        assertThat(captor.getValue().processId()).isEqualTo(processId);
        assertThat(NotificationMessageFactory.build(captor.getValue()).deepLink())
                .isEqualTo("/executive/semaphore?processId=" + processId);
    }
}
