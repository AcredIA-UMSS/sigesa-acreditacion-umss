package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.IndicatorAnalysis;

import java.util.List;
import java.util.UUID;

public interface GetIndicatorAnalysisUseCase {

    IndicatorAnalysis get(UUID indicatorId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope);
}
