package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorStateHistoryEntity;
import com.umss.sigesa.adapter.out.persistance.entity.ObservationEntity;
import com.umss.sigesa.adapter.out.persistance.entity.ProgramDashboardSummaryEntity;
import com.umss.sigesa.adapter.out.persistance.entity.ProgramPhaseSummaryEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import com.umss.sigesa.application.port.out.DashboardQueryPort;
import com.umss.sigesa.domain.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class JpaDashboardQueryAdapter implements DashboardQueryPort {

    private final ProgramDashboardSummaryJpaRepository summaryRepository;
    private final ObservationJpaRepository observationRepository;
    private final IndicatorJpaRepository indicatorJpaRepository;
    private final IndicatorStateHistoryJpaRepository historyRepository;
    private final SpringDataAccreditationProcessRepository processRepository;

    public JpaDashboardQueryAdapter(ProgramDashboardSummaryJpaRepository summaryRepository,
                                  ObservationJpaRepository observationRepository,
                                  IndicatorJpaRepository indicatorJpaRepository,
                                  IndicatorStateHistoryJpaRepository historyRepository,
                                  SpringDataAccreditationProcessRepository processRepository) {
        this.summaryRepository = summaryRepository;
        this.observationRepository = observationRepository;
        this.indicatorJpaRepository = indicatorJpaRepository;
        this.historyRepository = historyRepository;
        this.processRepository = processRepository;
    }

    private void updateSummaryDynamically(ProgramDashboardSummaryEntity summary) {
        UUID programId = summary.getProgramId();
        List<IndicatorEntity> indicators = indicatorJpaRepository.findByProgramId(programId);
        int total = indicators.size();
        if (total > 0) {
            int approved = 0;
            int rejected = 0;
            int pendingObs = (int) observationRepository.countByProgramIdAndStatusNot(programId, "RESOLVED");

            for (IndicatorEntity indicator : indicators) {
                IndicatorState state = historyRepository.findTopByIndicatorIdOrderByCreatedAtDesc(indicator.getId())
                        .map(IndicatorStateHistoryEntity::getNewState)
                        .orElse(IndicatorState.PENDIENTE);
                if (state == IndicatorState.APROBADO) {
                    approved++;
                } else if (state == IndicatorState.OBSERVADO) {
                    rejected++;
                }
            }

            double progress = (approved * 100.0) / total;
            progress = Math.round(progress * 100.0) / 100.0;

            summary.setTotalIndicators(total);
            summary.setOverallProgressPercentage(progress);
            summary.setApprovedEvidences(approved);
            summary.setRejectedEvidences(rejected);
            summary.setPendingObservations(pendingObs);

            if (summary.getPhases() != null) {
                for (ProgramPhaseSummaryEntity phaseSummary : summary.getPhases()) {
                    List<IndicatorEntity> phaseIndicators = indicatorJpaRepository.findByProgramIdAndPhaseOrder(programId, phaseSummary.getPhaseId());
                    if (!phaseIndicators.isEmpty()) {
                        int phaseTotal = phaseIndicators.size();
                        int phaseApproved = 0;
                        for (IndicatorEntity ind : phaseIndicators) {
                            IndicatorState state = historyRepository.findTopByIndicatorIdOrderByCreatedAtDesc(ind.getId())
                                    .map(IndicatorStateHistoryEntity::getNewState)
                                    .orElse(IndicatorState.PENDIENTE);
                            if (state == IndicatorState.APROBADO) {
                                phaseApproved++;
                            }
                        }
                        double phaseProgress = (phaseApproved * 100.0) / phaseTotal;
                        phaseProgress = Math.round(phaseProgress * 100.0) / 100.0;
                        phaseSummary.setPercentage(phaseProgress);
                        if (phaseProgress == 100.0) {
                            phaseSummary.setStatus("COMPLETED");
                        } else if (phaseProgress > 0.0) {
                            phaseSummary.setStatus("IN_PROCESO");
                        } else {
                            phaseSummary.setStatus("PENDIENTE");
                        }
                    } else {
                        phaseSummary.setPercentage(0.0);
                        phaseSummary.setStatus("PENDIENTE");
                    }
                }
            }
        }
    }

    @Override
    public CoordinatorKpiSection findCoordinatorKpi(UUID programId) {
        return summaryRepository.findById(programId)
                .map(summary -> {
                    updateSummaryDynamically(summary);
                    return toCoordinatorSection(summary);
                })
                .orElse(null);
    }

    @Override
    public TechnicianKpiSection findTechnicianKpi(UUID userId) {
        long pendingReview = observationRepository.countByStatus("EN_REVISION_TECNICA");
        long assignedIndicators = observationRepository.countDistinctIndicators();
        long openActions = observationRepository.countByStatus("PENDING_REMEDIATION") + observationRepository.countByStatus("PENDING_SUBSANACION");
        long available = observationRepository.countByStatus("EN_REVISION_TECNICA");

        List<ObservationEntity> recentEntities = observationRepository.findRecentEvaluations(PageRequest.of(0, 5));
        List<RecentEvaluation> recentEvaluations = recentEntities.stream()
                .map(o -> {
                    String programName = summaryRepository.findById(o.getProgramId())
                            .map(ProgramDashboardSummaryEntity::getProgramName)
                            .orElse("Systems Engineering");
                    return new RecentEvaluation(
                            o.getObservationId(),
                            programName,
                            o.getIssueDate().toString(),
                            o.getStatus()
                    );
                })
                .toList();

        return new TechnicianKpiSection(
                (int) pendingReview,
                (int) assignedIndicators,
                (int) openActions,
                (int) available,
                recentEvaluations
        );
    }

    @Override
    public ExecutiveKpiSection findExecutiveKpi() {
        List<ProgramDashboardSummaryEntity> summaries = summaryRepository.findAll();
        for (ProgramDashboardSummaryEntity summary : summaries) {
            updateSummaryDynamically(summary);
        }
        int totalPrograms = summaries.size();
        double averageProgress = summaries.stream()
                .mapToDouble(ProgramDashboardSummaryEntity::getOverallProgressPercentage)
                .average()
                .orElse(0.0);
        averageProgress = Math.round(averageProgress * 100.0) / 100.0;

        int criticalObs = summaries.stream()
                .mapToInt(ProgramDashboardSummaryEntity::getPendingObservations)
                .sum();

        List<ProgramTrafficLight> trafficLights = summaries.stream()
                .map(p -> {
                    String status = "VERDE";
                    if (p.getOverallProgressPercentage() < 50.0 || p.getPendingObservations() > 10) {
                        status = "ROJO";
                    } else if (p.getOverallProgressPercentage() < 80.0 || p.getPendingObservations() > 0) {
                        status = "AMARILLO";
                    }
                    return new ProgramTrafficLight(
                            p.getProgramId().toString(),
                            p.getProgramName(),
                            status,
                            p.getPendingObservations()
                    );
                })
                .toList();

        long alertProgramsCount = trafficLights.stream()
                .filter(t -> !"VERDE".equals(t.status()))
                .count();

        return new ExecutiveKpiSection(
                totalPrograms,
                averageProgress,
                criticalObs,
                (int) alertProgramsCount,
                trafficLights
        );
    }

    @Override
    public Page<ObservationSummary> findObservationDetails(UUID programId, Integer phaseId, String status, Pageable pageable) {
        Page<ObservationEntity> page = observationRepository.findByProgramIdAndFilters(programId, phaseId, status, pageable);
        return page.map(this::toObservationSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Stream<ObservationSummary> streamAllObservationsForReport(UUID programId, Integer phaseId) {
        Stream<ObservationEntity> entityStream = observationRepository.streamByProgramIdAndPhaseId(programId, phaseId);
        return entityStream.map(this::toObservationSummary);
    }

    private CoordinatorKpiSection toCoordinatorSection(ProgramDashboardSummaryEntity entity) {
        List<PhaseProgressSummary> phases = entity.getPhases().stream()
                .map(p -> new PhaseProgressSummary(p.getPhaseId(), p.getName(), p.getPercentage(), p.getStatus()))
                .toList();

        return new CoordinatorKpiSection(
                entity.getProgramId(),
                entity.getProgramName(),
                entity.getTotalIndicators(),
                entity.getOverallProgressPercentage(),
                entity.getApprovedEvidences(),
                entity.getRejectedEvidences(),
                entity.getPendingObservations(),
                phases,
                List.of(new BottleneckSummary("IND-102", "CRIT-3.1", 14))
        );
    }

    private ObservationSummary toObservationSummary(ObservationEntity entity) {
        long remainingDays = entity.getDueDate() != null
                ? ChronoUnit.DAYS.between(LocalDate.now(), entity.getDueDate())
                : 0L;

        return new ObservationSummary(
                entity.getObservationId(),
                entity.getIndicatorId(),
                entity.getIndicatorCode(),
                entity.getIndicatorTitle(),
                entity.getDescription(),
                entity.getIssueDate(),
                entity.getDueDate(),
                remainingDays,
                entity.getStatus(),
                entity.getRemediationUrl()
        );
    }
}
