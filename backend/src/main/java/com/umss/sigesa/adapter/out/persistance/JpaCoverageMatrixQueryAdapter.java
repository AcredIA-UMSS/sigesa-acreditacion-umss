package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.NormativeIndicatorJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataNormativeIndicatorRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataProgramRepository;
import com.umss.sigesa.application.port.out.CoverageMatrixQueryPort;
import com.umss.sigesa.domain.model.CoverageIndicatorInput;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaCoverageMatrixQueryAdapter implements CoverageMatrixQueryPort {

    private final SpringDataNormativeIndicatorRepository indicatorRepository;
    private final EvidenceJpaRepository evidenceRepository;
    private final SpringDataProgramRepository programRepository;

    public JpaCoverageMatrixQueryAdapter(
            SpringDataNormativeIndicatorRepository indicatorRepository,
            EvidenceJpaRepository evidenceRepository,
            SpringDataProgramRepository programRepository) {
        this.indicatorRepository = indicatorRepository;
        this.evidenceRepository = evidenceRepository;
        this.programRepository = programRepository;
    }

    @Override
    public List<CoverageIndicatorInput> findIndicatorRows(UUID processId) {
        List<NormativeIndicatorJpaEntity> indicators = indicatorRepository.findTreeByProcessId(processId);
        if (indicators.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = indicators.stream().map(NormativeIndicatorJpaEntity::getId).toList();
        Map<UUID, Integer> evidenceCounts = new HashMap<>();
        for (Object[] row : evidenceRepository.countGroupedByNormativeIndicatorId(ids)) {
            evidenceCounts.put((UUID) row[0], ((Number) row[1]).intValue());
        }
        return indicators.stream().map(indicator -> {
            var l3 = indicator.getLevel3Node();
            var l2 = l3.getLevel2Node();
            var l1 = l2.getLevel1Node();
            return new CoverageIndicatorInput(
                    l1.getId(),
                    l1.getName(),
                    l1.getOrder() == null ? 0 : l1.getOrder(),
                    l2.getId(),
                    l2.getName(),
                    l2.getOrder() == null ? 0 : l2.getOrder(),
                    l3.getId(),
                    l3.getName(),
                    l3.getOrder() == null ? 0 : l3.getOrder(),
                    indicator.getId(),
                    indicator.getCode(),
                    indicator.getDescription(),
                    indicator.getStatus(),
                    evidenceCounts.getOrDefault(indicator.getId(), 0),
                    indicator.getOrder() == null ? 0 : indicator.getOrder());
        }).toList();
    }

    @Override
    public Optional<String> findCareerName(UUID careerId) {
        if (careerId == null) {
            return Optional.empty();
        }
        return programRepository.findById(careerId).map(program -> program.getName());
    }
}
