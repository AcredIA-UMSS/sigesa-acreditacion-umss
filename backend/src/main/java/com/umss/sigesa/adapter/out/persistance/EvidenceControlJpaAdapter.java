package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorStateHistoryEntity;
import com.umss.sigesa.application.model.evidence.EvidenceControlItem;
import com.umss.sigesa.application.model.evidence.UploadableIndicator;
import com.umss.sigesa.application.port.out.EvidenceControlQueryPort;
import com.umss.sigesa.domain.model.IndicatorState;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class EvidenceControlJpaAdapter implements EvidenceControlQueryPort {

    private final IndicatorJpaRepository indicatorRepository;
    private final IndicatorStateHistoryJpaRepository historyRepository;
    private final EvidenceJpaRepository evidenceRepository;
    private final EvidenceVersionJpaRepository versionRepository;

    public EvidenceControlJpaAdapter(IndicatorJpaRepository indicatorRepository,
                                     IndicatorStateHistoryJpaRepository historyRepository,
                                     EvidenceJpaRepository evidenceRepository,
                                     EvidenceVersionJpaRepository versionRepository) {
        this.indicatorRepository = indicatorRepository;
        this.historyRepository = historyRepository;
        this.evidenceRepository = evidenceRepository;
        this.versionRepository = versionRepository;
    }

    @Override
    public List<EvidenceControlItem> listByProgramIdsAndStates(List<UUID> programIds, Set<IndicatorState> states) {
        List<IndicatorEntity> indicators = (programIds == null || programIds.isEmpty())
                ? indicatorRepository.findAll()
                : indicatorRepository.findByProgramIdIn(programIds);

        List<EvidenceControlItem> items = new ArrayList<>();
        for (IndicatorEntity indicator : indicators) {
            IndicatorState currentState = getCurrentState(indicator.getId());
            if (states != null && !states.isEmpty() && !states.contains(currentState)) {
                continue;
            }
            items.add(toItem(indicator, currentState));
        }
        return items;
    }

    @Override
    public Optional<EvidenceControlItem> findByIndicatorId(UUID indicatorId) {
        if (indicatorId == null) {
            return Optional.empty();
        }
        return indicatorRepository.findById(indicatorId)
                .map(indicator -> toItem(indicator, getCurrentState(indicator.getId())));
    }

    @Override
    public List<UploadableIndicator> listUploadableByProgramIds(List<UUID> programIds, Set<IndicatorState> states) {
        if (programIds == null || programIds.isEmpty()) {
            return List.of();
        }
        List<IndicatorEntity> indicators = indicatorRepository.findByProgramIdIn(programIds);
        List<UploadableIndicator> items = new ArrayList<>();
        for (IndicatorEntity indicator : indicators) {
            IndicatorState currentState = getCurrentState(indicator.getId());
            if (states != null && !states.isEmpty() && !states.contains(currentState)) {
                continue;
            }
            items.add(toUploadable(indicator, currentState));
        }
        return items;
    }

    private UploadableIndicator toUploadable(IndicatorEntity indicator, IndicatorState currentState) {
        String code = blankToFallback(indicator.getCode(), "IND");
        String title = blankToFallback(indicator.getTitle(), shortId(indicator.getId()));
        String criterionCode = blankToFallback(indicator.getCriterionCode(), "CRIT");
        String criterionTitle = blankToFallback(
                indicator.getCriterionTitle(),
                shortId(indicator.getCriterionId()));
        return new UploadableIndicator(
                indicator.getId(),
                code,
                title,
                indicator.getCriterionId(),
                criterionCode,
                criterionTitle,
                currentState);
    }

    private static String blankToFallback(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private static String shortId(UUID id) {
        if (id == null) {
            return "—";
        }
        String s = id.toString();
        return s.length() >= 8 ? s.substring(0, 8) : s;
    }

    private IndicatorState getCurrentState(UUID indicatorId) {
        return historyRepository.findTopByIndicatorIdOrderByCreatedAtDesc(indicatorId)
                .map(IndicatorStateHistoryEntity::getNewState)
                .orElse(IndicatorState.PENDIENTE);
    }

    private EvidenceControlItem toItem(IndicatorEntity indicator, IndicatorState currentState) {
        Optional<EvidenceEntity> evidenceOpt = evidenceRepository.findByIndicatorId(indicator.getId());
        if (evidenceOpt.isEmpty()) {
            return new EvidenceControlItem(
                    indicator.getId(),
                    indicator.getProgramId(),
                    indicator.getCriterionId(),
                    indicator.getPhaseId(),
                    currentState,
                    null,
                    null,
                    null,
                    null,
                    null);
        }

        EvidenceEntity evidence = evidenceOpt.get();
        Optional<EvidenceVersionEntity> versionOpt = evidence.getLatestVersionId() == null
                ? Optional.empty()
                : versionRepository.findById(evidence.getLatestVersionId());

        return versionOpt.map(version -> new EvidenceControlItem(
                        indicator.getId(),
                        indicator.getProgramId(),
                        version.getCriterionId() != null ? version.getCriterionId() : indicator.getCriterionId(),
                        indicator.getPhaseId(),
                        currentState,
                        evidence.getId(),
                        version.getVersionNumber(),
                        version.getContentHash(),
                        version.getDescription(),
                        version.getCreatedAt() != null ? version.getCreatedAt() : evidence.getCreatedAt()))
                .orElseGet(() -> new EvidenceControlItem(
                        indicator.getId(),
                        indicator.getProgramId(),
                        indicator.getCriterionId(),
                        indicator.getPhaseId(),
                        currentState,
                        evidence.getId(),
                        null,
                        null,
                        null,
                        evidence.getCreatedAt()));
    }
}