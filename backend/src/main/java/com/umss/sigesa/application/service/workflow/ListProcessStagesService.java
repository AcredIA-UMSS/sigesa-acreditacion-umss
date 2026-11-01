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
    private final MethodologicalStageBootstrapper stageBootstrapper;

    public ListProcessStagesService(ProcessQueryPort processQueryPort,
                                    MethodologicalStagePort stagePort,
                                    MethodologicalStageBootstrapper stageBootstrapper) {
        this.processQueryPort = processQueryPort;
        this.stagePort = stagePort;
        this.stageBootstrapper = stageBootstrapper;
    }

    @Override
    public List<MethodologicalStage> listStages(UUID processId) {
        processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException("Proceso no encontrado: " + processId));
        stageBootstrapper.bootstrapForProcess(processId);
        return stagePort.findByProcessIdOrderByOrder(processId);
    }
}
