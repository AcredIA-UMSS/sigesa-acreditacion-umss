package com.umss.sigesa.adapter.out.notification;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorObservationJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level1NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataIndicatorObservationRepository;
import com.umss.sigesa.application.port.in.EnqueueDomainNotificationUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.notification.DomainNotificationCommand;
import com.umss.sigesa.domain.model.notification.NotificationEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Daily producer for {@link NotificationEventType#DeadlineApproaching}.
 * Reads real {@code indicator_observation.due_date} rows and skips CLOSED/ARCHIVED processes by processId.
 */
@Component
public class DeadlineDelayNotificationJob {

    private static final Logger log = LoggerFactory.getLogger(DeadlineDelayNotificationJob.class);
    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;
    private static final Set<String> CLOSED_PROCESS = Set.of(
            ProcessStatus.CLOSED.name(), ProcessStatus.ARCHIVED.name());

    private final SpringDataIndicatorObservationRepository observationRepository;
    private final ProcessQueryPort processQueryPort;
    private final EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase;
    private final int deadlineDays;

    public DeadlineDelayNotificationJob(
            SpringDataIndicatorObservationRepository observationRepository,
            ProcessQueryPort processQueryPort,
            EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase,
            @Value("${sigesa.notify.deadline-days:3}") int deadlineDays) {
        this.observationRepository = observationRepository;
        this.processQueryPort = processQueryPort;
        this.enqueueDomainNotificationUseCase = enqueueDomainNotificationUseCase;
        this.deadlineDays = Math.max(deadlineDays, 1);
    }

    @Scheduled(cron = "${sigesa.notify.deadline-cron:0 0 7 * * *}")
    public void runDaily() {
        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(deadlineDays);
        List<IndicatorObservationJpaEntity> approaching =
                observationRepository.findApproachingDeadlines(today, until);
        if (approaching.isEmpty()) {
            return;
        }

        Set<UUID> closedProcessIds = processQueryPort.findAllSummaryItems().stream()
                .filter(item -> CLOSED_PROCESS.contains(item.status()))
                .map(ProcessQueryPort.ProcessListItem::id)
                .collect(Collectors.toSet());

        String dayKey = today.format(DAY);
        int enqueued = 0;
        for (IndicatorObservationJpaEntity observation : approaching) {
            Level1NodeJpaEntity level1 = observation.getIndicator()
                    .getLevel3Node()
                    .getLevel2Node()
                    .getLevel1Node();
            UUID processId = level1.getProcess().getId();
            UUID programId = level1.getProcess().getCareerId();
            UUID indicatorId = observation.getIndicator().getId();

            if (closedProcessIds.contains(processId)) {
                continue;
            }

            Map<String, String> payload = new HashMap<>();
            payload.put("indicatorId", indicatorId.toString());
            payload.put("observationId", observation.getId().toString());
            payload.put("processId", processId.toString());
            payload.put("programId", programId.toString());
            payload.put("deadlineDay", dayKey);
            if (observation.getDueDate() != null) {
                payload.put("dueDate", observation.getDueDate().toString());
            }

            enqueueDomainNotificationUseCase.enqueue(new DomainNotificationCommand(
                    NotificationEventType.DeadlineApproaching,
                    observation.getId(),
                    programId,
                    processId,
                    payload));
            enqueued++;
        }
        log.info("DeadlineDelayNotificationJob scanned={} enqueuedApprox={}", approaching.size(), enqueued);
    }
}
