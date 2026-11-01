package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.IndicatorAnalysis;
import com.umss.sigesa.domain.model.UpsertIndicatorAnalysisCommand;

public interface UpsertIndicatorAnalysisUseCase {

    IndicatorAnalysis upsert(UpsertIndicatorAnalysisCommand command);
}
