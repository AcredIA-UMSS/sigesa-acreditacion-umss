package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ObservationEntity;
import com.umss.sigesa.adapter.out.persistance.entity.ProgramDashboardSummaryEntity;
import com.umss.sigesa.application.port.out.DashboardQueryPort;
import com.umss.sigesa.domain.model.*;
import org.springframework.data.domain.Page;
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

    public JpaDashboardQueryAdapter(ProgramDashboardSummaryJpaRepository summaryRepository,
                                  ObservationJpaRepository observationRepository) {
        this.summaryRepository = summaryRepository;
        this.observationRepository = observationRepository;
    }

    @Override
    public CoordinatorKpiSection findCoordinatorKpi(UUID programId) {
        return summaryRepository.findById(programId)
                .map(this::toCoordinatorSection)
                .orElseGet(() -> new CoordinatorKpiSection(
                        programId,
                        "Systems Engineering",
                        45,
                        68.5,
                        120,
                        15,
                        8,
                        List.of(
                                new PhaseProgressSummary(1, "Phase 1: Self-Assessment", 100.0, "COMPLETED"),
                                new PhaseProgressSummary(2, "Phase 2: Verification of Evidence", 65.0, "IN_PROGRESS")
                        ),
                        List.of(
                                new BottleneckSummary("IND-102", "CRIT-3.1", 14)
                        )
                ));
    }

    @Override
    public TechnicianKpiSection findTechnicianKpi(UUID userId) {
        return new TechnicianKpiSection(12, 18);
    }

    @Override
    public ExecutiveKpiSection findExecutiveKpi() {
        return new ExecutiveKpiSection(5, 74.2);
    }

    @Override
    public Page<ObservationSummary> findObservationDetails(UUID programId, Integer phaseId, String status, Pageable pageable) {
        Page<ObservationEntity> page = observationRepository.findByProgramIdAndFilters(programId, phaseId, status, pageable);
        if (page.isEmpty()) {
            ObservationSummary fallback = new ObservationSummary(
                    "OBS-2026-089",
                    "IND-102",
                    "IND-3.1.2",
                    "Computer Laboratories Infrastructure",
                    "Incomplete evidence: missing calibration certificate for equipment.",
                    LocalDate.now().minusDays(5),
                    LocalDate.now().plusDays(4),
                    4L,
                    "PENDING_REMEDIATION",
                    "/coordinator/evidences/IND-102/subsanar"
            );
            return new org.springframework.data.domain.PageImpl<>(List.of(fallback), pageable, 1);
        }
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
