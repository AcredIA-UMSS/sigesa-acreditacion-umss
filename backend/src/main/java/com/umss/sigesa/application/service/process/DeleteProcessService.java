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

        if (!ProcessStatus.ACTIVE.name().equals(process.getStatus())) {
            throw new ProcessNotDeletableException(
                    "Solo se pueden eliminar procesos en estado ACTIVE.");
        }

        if (evaluationMetricsPort.countIndicatorsWithEvidenceByProcessId(processId) > 0) {
            throw new ProcessHasEvidenceException(
                    "El proceso tiene evidencias cargadas y no puede eliminarse.");
        }

        process.setStatus(ProcessStatus.ARCHIVED.name());
        accreditationProcessPort.save(process);
    }
}
