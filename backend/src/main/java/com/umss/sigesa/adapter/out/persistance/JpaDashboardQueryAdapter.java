package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorStateHistoryEntity;
import com.umss.sigesa.adapter.out.persistance.entity.ObservationEntity;
import com.umss.sigesa.adapter.out.persistance.entity.ProgramDashboardSummaryEntity;
import com.umss.sigesa.adapter.out.persistance.entity.ProgramPhaseSummaryEntity;
import com.umss.sigesa.adapter.out.persistance.entity.ProgramJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataNormativeIndicatorRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataProgramRepository;
import com.umss.sigesa.application.port.out.DashboardQueryPort;
import com.umss.sigesa.domain.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class JpaDashboardQueryAdapter implements DashboardQueryPort {

    private static final Set<String> CLOSED_OBSERVATION_STATUSES = Set.of(
            "RESOLVED", "CERRADA", "APROBADO", "CLOSED");

    private final ProgramDashboardSummaryJpaRepository summaryRepository;
    private final ObservationJpaRepository observationRepository;
    private final IndicatorJpaRepository indicatorJpaRepository;
    private final IndicatorStateHistoryJpaRepository historyRepository;
    private final SpringDataAccreditationProcessRepository processRepository;
    private final SpringDataProgramRepository programRepository;
    private final SpringDataNormativeIndicatorRepository normativeIndicatorRepository;

    public JpaDashboardQueryAdapter(ProgramDashboardSummaryJpaRepository summaryRepository,
                                  ObservationJpaRepository observationRepository,
                                  IndicatorJpaRepository indicatorJpaRepository,
                                  IndicatorStateHistoryJpaRepository historyRepository,
                                  SpringDataAccreditationProcessRepository processRepository,
                                  SpringDataProgramRepository programRepository,
                                  SpringDataNormativeIndicatorRepository normativeIndicatorRepository) {
        this.summaryRepository = summaryRepository;
        this.observationRepository = observationRepository;
        this.indicatorJpaRepository = indicatorJpaRepository;
        this.historyRepository = historyRepository;
        this.processRepository = processRepository;
        this.programRepository = programRepository;
        this.normativeIndicatorRepository = normativeIndicatorRepository;
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
        Map<UUID, ProgramJpaEntity> programs = new LinkedHashMap<>();
        for (ProgramJpaEntity program : programRepository.searchActive(null)) {
            programs.put(program.getId(), program);
        }

        Map<UUID, int[]> indicatorCounts = loadNormativeIndicatorCounts();
        Map<UUID, int[]> observationCounts = loadObservationCounts();
        Map<UUID, ProgramDashboardSummaryEntity> summaries = new HashMap<>();
        for (ProgramDashboardSummaryEntity summary : summaryRepository.findAll()) {
            summaries.put(summary.getProgramId(), summary);
        }

        Set<UUID> careerIds = new LinkedHashSet<>();
        careerIds.addAll(programs.keySet());
        careerIds.addAll(summaries.keySet());

        List<CareerSemaphoreInput> inputs = new ArrayList<>();
        for (UUID careerId : careerIds) {
            inputs.add(toSemaphoreInput(
                    careerId,
                    programs.get(careerId),
                    summaries.get(careerId),
                    indicatorCounts.getOrDefault(careerId, new int[3]),
                    observationCounts.getOrDefault(careerId, new int[2])));
        }
        inputs.sort(Comparator.comparing(CareerSemaphoreInput::faculty, Comparator.nullsLast(String::compareTo))
                .thenComparing(CareerSemaphoreInput::name, Comparator.nullsLast(String::compareTo)));
        return ExecutiveSemaphoreAssembler.assemble(inputs);
    }

    /**
     * @return [total, approved, observed] por carrera
     */
    private Map<UUID, int[]> loadNormativeIndicatorCounts() {
        Map<UUID, int[]> counts = new HashMap<>();
        for (Object[] row : normativeIndicatorRepository.countActiveByCareerAndStatus()) {
            UUID careerId = (UUID) row[0];
            String status = row[1] == null ? "" : row[1].toString();
            int amount = ((Number) row[2]).intValue();
            int[] bucket = counts.computeIfAbsent(careerId, ignored -> new int[3]);
            bucket[0] += amount;
            if (IndicatorState.APROBADO.name().equals(status)) {
                bucket[1] += amount;
            } else if (IndicatorState.OBSERVADO.name().equals(status)) {
                bucket[2] += amount;
            }
        }
        return counts;
    }

    /**
     * @return [abiertas, vencidas] por carrera
     */
    private Map<UUID, int[]> loadObservationCounts() {
        Map<UUID, int[]> counts = new HashMap<>();
        LocalDate today = LocalDate.now();
        for (ObservationEntity observation : observationRepository.findAll()) {
            if (observation.getProgramId() == null || isClosedObservation(observation.getStatus())) {
                continue;
            }
            int[] bucket = counts.computeIfAbsent(observation.getProgramId(), ignored -> new int[2]);
            bucket[0]++;
            if (observation.getDueDate() != null && observation.getDueDate().isBefore(today)) {
                bucket[1]++;
            }
        }
        return counts;
    }

    private static boolean isClosedObservation(String status) {
        return status != null && CLOSED_OBSERVATION_STATUSES.contains(status);
    }

    private CareerSemaphoreInput toSemaphoreInput(
            UUID careerId,
            ProgramJpaEntity program,
            ProgramDashboardSummaryEntity summary,
            int[] indicators,
            int[] observations) {
        int total = indicators[0];
        int approved = indicators[1];
        int observed = indicators[2];
        int pending = observations[0];
        int overdue = observations[1];
        double progress;

        if (total > 0) {
            progress = Math.round((approved * 10000.0) / total) / 100.0;
            if (pending == 0) {
                pending = observed;
            }
        } else if (summary != null) {
            total = summary.getTotalIndicators();
            progress = summary.getOverallProgressPercentage();
            if (pending == 0) {
                pending = summary.getPendingObservations();
            }
        } else {
            progress = 0.0;
        }

        String name = program != null ? program.getName()
                : summary != null ? summary.getProgramName()
                : careerId.toString();
        String faculty = program != null && program.getFaculty() != null && !program.getFaculty().isBlank()
                ? program.getFaculty()
                : "Sin facultad";
        return new CareerSemaphoreInput(
                careerId.toString(),
                name,
                faculty,
                total,
                progress,
                pending,
                overdue);
    }

    @Override
    public List<ObservationSummary> findObservationsByProgram(UUID programId) {
        return observationRepository.findByProgramIdOrderByDueDateAsc(programId).stream()
                .map(this::toObservationSummary)
                .toList();
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
