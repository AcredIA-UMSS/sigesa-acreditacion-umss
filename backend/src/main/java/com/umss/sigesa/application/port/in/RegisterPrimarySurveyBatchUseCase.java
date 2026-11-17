package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.PrimarySurveyBatch;
import com.umss.sigesa.domain.model.RegisterPrimarySurveyBatchCommand;

public interface RegisterPrimarySurveyBatchUseCase {

    PrimarySurveyBatch register(RegisterPrimarySurveyBatchCommand command);
}
