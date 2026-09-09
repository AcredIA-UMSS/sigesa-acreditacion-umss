package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.ListProcessStagesUseCase;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.MethodologicalStage;

import java.util.List;
import java.util.UUID;

public class ListProcessStagesService implements ListProcessStagesUseCase {

    private final ProcessQueryPort processQueryPort;
    private final MethodologicalStagePort stagePort;

    public ListProcessStagesService(ProcessQueryPort processQueryPort,
                                    MethodologicalStagePort stagePort) {
        this.processQueryPort = processQueryPort;
        this.stagePort = stagePort;
    }

    @Override
    public List<MethodologicalStage> listStages(UUID processId) {
        processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException("Proceso no encontrado: " + processId));
        return stagePort.findByProcessIdOrderByOrder(processId);
    }
}
