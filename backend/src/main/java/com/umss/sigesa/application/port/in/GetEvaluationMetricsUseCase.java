package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.EvaluationMetrics;

import java.util.List;
import java.util.UUID;

public interface GetEvaluationMetricsUseCase {

    EvaluationMetrics get(UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope);
}
