package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.in.EnqueueDomainNotificationUseCase;
import com.umss.sigesa.application.port.in.RemoveProcessResponsibleUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessResponsibleAssignment;
import com.umss.sigesa.domain.model.notification.DomainNotificationCommand;
import com.umss.sigesa.domain.model.notification.NotificationEventType;

import java.util.Map;
import java.util.UUID;

public class RemoveProcessResponsibleService implements RemoveProcessResponsibleUseCase {

    private final ProcessQueryPort processQueryPort;
    private final ProcessResponsiblePort processResponsiblePort;
    private final ProcessStructureGuard processStructureGuard;
    private final EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase;

    public RemoveProcessResponsibleService(ProcessQueryPort processQueryPort,
                                           ProcessResponsiblePort processResponsiblePort,
                                           ProcessStructureGuard processStructureGuard,
                                           EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase) {
        this.processQueryPort = processQueryPort;
        this.processResponsiblePort = processResponsiblePort;
        this.processStructureGuard = processStructureGuard;
        this.enqueueDomainNotificationUseCase = enqueueDomainNotificationUseCase;
    }

    @Override
    public void remove(UUID processId) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        processStructureGuard.ensureProcessActive(process);

        UUID removedUserId = processResponsiblePort.findActiveByProcessId(processId)
                .map(ProcessResponsibleAssignment::getUserId)
                .orElse(null);

        processResponsiblePort.revokeActiveByProcessId(processId);

        if (removedUserId != null) {
            enqueueDomainNotificationUseCase.enqueue(new DomainNotificationCommand(
                    NotificationEventType.ProcessUnassigned,
                    processId,
                    process.getCareerId(),
                    processId,
                    Map.of(
                            "processId", processId.toString(),
                            "programId", process.getCareerId() != null ? process.getCareerId().toString() : "",
                            "removedUserId", removedUserId.toString(),
                            "occurrenceId", UUID.randomUUID().toString()
                    )));
        }
    }
}
