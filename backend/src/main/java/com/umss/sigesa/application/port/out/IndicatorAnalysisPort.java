package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.IndicatorAnalysis;

import java.util.Optional;
import java.util.UUID;

public interface IndicatorAnalysisPort {

    Optional<IndicatorAnalysis> findByIndicatorId(UUID indicatorId);

    IndicatorAnalysis save(IndicatorAnalysis analysis);

    long countCompleteByProcessId(UUID processId);
}
