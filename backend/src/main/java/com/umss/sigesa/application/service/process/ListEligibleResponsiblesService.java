package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.in.ListEligibleResponsiblesUseCase;
import com.umss.sigesa.application.port.in.ListUsersUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ListEligibleResponsiblesService implements ListEligibleResponsiblesUseCase {

    private final ProcessQueryPort processQueryPort;
    private final ListUsersUseCase listUsersUseCase;
    private final ProcessResponsiblePort processResponsiblePort;

    public ListEligibleResponsiblesService(ProcessQueryPort processQueryPort,
                                           ListUsersUseCase listUsersUseCase,
                                           ProcessResponsiblePort processResponsiblePort) {
        this.processQueryPort = processQueryPort;
        this.listUsersUseCase = listUsersUseCase;
        this.processResponsiblePort = processResponsiblePort;
    }

    @Override
    public List<EligibleResponsible> listEligible(UUID processId) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));

        UUID currentResponsibleId = processResponsiblePort.findActiveByProcessId(processId)
                .map(assignment -> assignment.getUserId())
                .orElse(null);
        Set<UUID> occupiedUserIds = processResponsiblePort.findUserIdsWithActiveAssignment();

        return listUsersUseCase.list("CC", "ACTIVE").stream()
                .filter(user -> user.programIds().contains(process.getCareerId()))
                .filter(user -> !occupiedUserIds.contains(user.userId())
                        || user.userId().equals(currentResponsibleId))
                .map(user -> new EligibleResponsible(user.userId(), user.fullName(), user.email()))
                .toList();
    }
}
