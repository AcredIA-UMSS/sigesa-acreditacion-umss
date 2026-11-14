package com.umss.sigesa.application.service.notification;

import com.umss.sigesa.application.port.in.CloseAccreditationProcessUseCase;
import com.umss.sigesa.application.port.in.EnqueueDomainNotificationUseCase;
import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.notification.DomainNotificationCommand;
import com.umss.sigesa.domain.model.notification.NotificationEventType;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CloseAccreditationProcessService implements CloseAccreditationProcessUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("JD", "TD", "EE");

    private final AccreditationProcessPort processPort;
    private final EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase;

    public CloseAccreditationProcessService(
            AccreditationProcessPort processPort,
            EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase) {
        this.processPort = processPort;
        this.enqueueDomainNotificationUseCase = enqueueDomainNotificationUseCase;
    }

    @Override
    public void close(UUID processId, UUID actorId, String actorRole) {
        assertRole(actorRole);

        AccreditationProcess process = processPort.findById(processId)
                .orElseThrow(() -> new ProcessNotFoundException("Proceso no encontrado: " + processId));

        if (ProcessStatus.ARCHIVED.name().equals(process.getStatus())) {
            throw new ProcessNotFoundException("Proceso archivado no puede cerrarse: " + processId);
        }

        if (!ProcessStatus.CLOSED.name().equals(process.getStatus())) {
            process.setStatus(ProcessStatus.CLOSED.name());
            processPort.save(process);
        }

        enqueueDomainNotificationUseCase.enqueue(new DomainNotificationCommand(
                NotificationEventType.ProcessClosed,
                processId,
                process.getCareerId(),
                processId,
                Map.of(
                        "processId", processId.toString(),
                        "programId", process.getCareerId() != null ? process.getCareerId().toString() : "",
                        "actorId", actorId != null ? actorId.toString() : "")));
    }

    private static void assertRole(String actorRole) {
        String normalized = actorRole != null ? actorRole.trim().toUpperCase(Locale.ROOT) : "";
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new InvalidRoleException("Rol no autorizado para cerrar el proceso de acreditación.");
        }
    }
}
