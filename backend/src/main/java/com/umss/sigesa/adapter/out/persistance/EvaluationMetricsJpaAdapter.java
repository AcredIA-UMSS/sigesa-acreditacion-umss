package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.repository.SpringDataNormativeIndicatorRepository;
import com.umss.sigesa.application.port.out.EvaluationMetricsPort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EvaluationMetricsJpaAdapter implements EvaluationMetricsPort {

    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final SpringDataNormativeIndicatorRepository indicatorRepository;

    @Override
    @Transactional(readOnly = true)
    public long countIndicatorsByProcessId(UUID processId) {
        return hierarchyQueryPort.countIndicatorsByProcessId(processId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countIndicatorsWithEvidenceByProcessId(UUID processId) {
        return indicatorRepository.countWithEvidenceByProcessId(processId);
    }

    @Override
    @Transactional(readOnly = true)
    public double evidenceCompletenessRatio(UUID processId) {
        long total = countIndicatorsByProcessId(processId);
        if (total == 0) {
            return 0.0d;
        }
        return (double) countIndicatorsWithEvidenceByProcessId(processId) / total;
    }
}
