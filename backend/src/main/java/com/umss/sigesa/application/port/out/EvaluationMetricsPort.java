package com.umss.sigesa.application.port.out;

import java.util.UUID;

public interface EvaluationMetricsPort {

    long countIndicatorsByProcessId(UUID processId);

    long countIndicatorsWithEvidenceByProcessId(UUID processId);

    double evidenceCompletenessRatio(UUID processId);
}
