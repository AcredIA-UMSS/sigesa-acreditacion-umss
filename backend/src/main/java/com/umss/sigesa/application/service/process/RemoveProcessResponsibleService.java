package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.in.RemoveProcessResponsibleUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;

import java.util.UUID;

public class RemoveProcessResponsibleService implements RemoveProcessResponsibleUseCase {

    private final ProcessQueryPort processQueryPort;
    private final ProcessResponsiblePort processResponsiblePort;
    private final ProcessStructureGuard processStructureGuard;

    public RemoveProcessResponsibleService(ProcessQueryPort processQueryPort,
                                           ProcessResponsiblePort processResponsiblePort,
                                           ProcessStructureGuard processStructureGuard) {
        this.processQueryPort = processQueryPort;
        this.processResponsiblePort = processResponsiblePort;
        this.processStructureGuard = processStructureGuard;
    }

    @Override
    public void remove(UUID processId) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        processStructureGuard.ensureProcessActive(process);
        processResponsiblePort.revokeActiveByProcessId(processId);
    }
}
