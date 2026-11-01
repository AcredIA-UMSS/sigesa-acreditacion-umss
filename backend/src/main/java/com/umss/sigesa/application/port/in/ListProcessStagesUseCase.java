package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.MethodologicalStage;

import java.util.List;
import java.util.UUID;

public interface ListProcessStagesUseCase {

    List<MethodologicalStage> listStages(UUID processId);
}
