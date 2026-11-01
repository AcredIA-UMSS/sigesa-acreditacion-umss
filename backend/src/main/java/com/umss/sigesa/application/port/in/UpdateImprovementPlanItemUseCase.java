package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ImprovementPlanItem;
import com.umss.sigesa.domain.model.UpdateImprovementPlanItemCommand;

public interface UpdateImprovementPlanItemUseCase {

    ImprovementPlanItem update(UpdateImprovementPlanItemCommand command);
}
