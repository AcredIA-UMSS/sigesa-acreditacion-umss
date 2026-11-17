package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.CreateImprovementPlanItemCommand;
import com.umss.sigesa.domain.model.ImprovementPlanItem;

public interface CreateImprovementPlanItemUseCase {

    ImprovementPlanItem create(CreateImprovementPlanItemCommand command);
}
