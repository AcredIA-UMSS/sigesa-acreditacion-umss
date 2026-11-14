package com.umss.sigesa.adapter.out.notification;

import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorObservationJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level1NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level2NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level3NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.NormativeIndicatorJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataIndicatorObservationRepository;
import com.umss.sigesa.application.port.in.EnqueueDomainNotificationUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.notification.DomainNotificationCommand;
import com.umss.sigesa.domain.model.notification.NotificationEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeadlineDelayNotificationJobTest {

    @Mock
    private SpringDataIndicatorObservationRepository observationRepository;
    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase;

    @Test
    @DisplayName("Enqueues DeadlineApproaching for ACTIVE process and skips CLOSED processId")
    void enqueuesForOpenProcessesOnly() {
        UUID openProgram = UUID.randomUUID();
        UUID closedProgram = UUID.randomUUID();
        UUID openProcessId = UUID.randomUUID();
        UUID closedProcessId = UUID.randomUUID();
        UUID openObservationId = UUID.randomUUID();

        IndicatorObservationJpaEntity open = observation(
                openObservationId, openProgram, openProcessId, LocalDate.now().plusDays(1));
        IndicatorObservationJpaEntity closedObs = observation(
                UUID.randomUUID(), closedProgram, closedProcessId, LocalDate.now().plusDays(2));

        when(observationRepository.findApproachingDeadlines(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(open, closedObs));
        when(processQueryPort.findAllSummaryItems()).thenReturn(List.of(
                new ProcessQueryPort.ProcessListItem(
                        openProcessId, openProgram, UUID.randomUUID(),
                        ProcessStatus.ACTIVE.name(), LocalDateTime.now()),
                new ProcessQueryPort.ProcessListItem(
                        closedProcessId, closedProgram, UUID.randomUUID(),
                        ProcessStatus.CLOSED.name(), LocalDateTime.now())));

        DeadlineDelayNotificationJob job = new DeadlineDelayNotificationJob(
                observationRepository, processQueryPort, enqueueDomainNotificationUseCase, 3);

        job.runDaily();

        ArgumentCaptor<DomainNotificationCommand> captor =
                ArgumentCaptor.forClass(DomainNotificationCommand.class);
        verify(enqueueDomainNotificationUseCase).enqueue(captor.capture());
        DomainNotificationCommand command = captor.getValue();
        assertThat(command.eventType()).isEqualTo(NotificationEventType.DeadlineApproaching);
        assertThat(command.programId()).isEqualTo(openProgram);
        assertThat(command.processId()).isEqualTo(openProcessId);
        assertThat(command.aggregateId()).isEqualTo(openObservationId);
        assertThat(command.payloadValue("observationId")).isEqualTo(openObservationId.toString());
        assertThat(command.payloadValue("processId")).isEqualTo(openProcessId.toString());
    }

    @Test
    @DisplayName("No-op when no approaching deadlines")
    void noOpWhenEmpty() {
        when(observationRepository.findApproachingDeadlines(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        DeadlineDelayNotificationJob job = new DeadlineDelayNotificationJob(
                observationRepository, processQueryPort, enqueueDomainNotificationUseCase, 3);

        job.runDaily();

        verify(enqueueDomainNotificationUseCase, never()).enqueue(any());
        verify(processQueryPort, never()).findAllSummaryItems();
    }

    private static IndicatorObservationJpaEntity observation(
            UUID observationId, UUID programId, UUID processId, LocalDate dueDate) {
        AccreditationProcessJpaEntity process = new AccreditationProcessJpaEntity();
        process.setId(processId);
        process.setCareerId(programId);

        Level1NodeJpaEntity level1 = new Level1NodeJpaEntity();
        level1.setId(UUID.randomUUID());
        level1.setProcess(process);

        Level2NodeJpaEntity level2 = new Level2NodeJpaEntity();
        level2.setId(UUID.randomUUID());
        level2.setLevel1Node(level1);

        Level3NodeJpaEntity level3 = new Level3NodeJpaEntity();
        level3.setId(UUID.randomUUID());
        level3.setLevel2Node(level2);

        NormativeIndicatorJpaEntity indicator = new NormativeIndicatorJpaEntity();
        indicator.setId(UUID.randomUUID());
        indicator.setLevel3Node(level3);

        IndicatorObservationJpaEntity entity = new IndicatorObservationJpaEntity();
        entity.setId(observationId);
        entity.setIndicator(indicator);
        entity.setAuthorId(UUID.randomUUID());
        entity.setAuthorRole("TD");
        entity.setBody("Observación con plazo próximo");
        entity.setStatus("OPEN");
        entity.setDueDate(dueDate);
        entity.setCreatedAt(LocalDateTime.now().minusDays(2));
        entity.setUpdatedAt(LocalDateTime.now().minusDays(2));
        return entity;
    }
}
