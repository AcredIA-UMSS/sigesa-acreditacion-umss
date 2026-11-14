package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.in.DeleteProcessUseCase;
import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.application.port.out.EvaluationMetricsPort;
import com.umss.sigesa.domain.exception.ProcessHasEvidenceException;
import com.umss.sigesa.domain.exception.ProcessNotDeletableException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteProcessService implements DeleteProcessUseCase {

    private final AccreditationProcessPort accreditationProcessPort;
    private final EvaluationMetricsPort evaluationMetricsPort;

    public DeleteProcessService(AccreditationProcessPort accreditationProcessPort,
                                EvaluationMetricsPort evaluationMetricsPort) {
        this.accreditationProcessPort = accreditationProcessPort;
        this.evaluationMetricsPort = evaluationMetricsPort;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID processId) {
        AccreditationProcess process = accreditationProcessPort.findById(processId)
                .orElseThrow(() -> new ProcessNotFoundException("Proceso no encontrado: " + processId));

        String status = process.getStatus();
        if (ProcessStatus.ARCHIVED.name().equals(status)) {
            throw new ProcessNotDeletableException("El proceso ya está archivado.");
        }

        boolean isActive = ProcessStatus.ACTIVE.name().equals(status);
        boolean isClosed = ProcessStatus.CLOSED.name().equals(status);
        if (!isActive && !isClosed) {
            throw new ProcessNotDeletableException(
                    "Solo se pueden eliminar procesos activos o cerrados (desactivados).");
        }

        if (isActive && evaluationMetricsPort.countIndicatorsWithEvidenceByProcessId(processId) > 0) {
            throw new ProcessHasEvidenceException(
                    "El proceso activo tiene evidencias cargadas y no puede eliminarse.");
        }

        process.setStatus(ProcessStatus.ARCHIVED.name());
        accreditationProcessPort.save(process);
    }
}
