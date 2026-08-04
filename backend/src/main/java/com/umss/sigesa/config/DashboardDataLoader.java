package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.ProgramDashboardSummaryJpaRepository;
import com.umss.sigesa.adapter.out.persistance.ObservationJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.ProgramDashboardSummaryEntity;
import com.umss.sigesa.adapter.out.persistance.entity.ProgramPhaseSummaryEntity;
import com.umss.sigesa.adapter.out.persistance.entity.ObservationEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Role;
import jakarta.persistence.EntityManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Profile("!prod")
@Order(105)
public class DashboardDataLoader implements ApplicationRunner {

    private static final Object LOCK = new Object();

    private final ProgramDashboardSummaryJpaRepository summaryRepository;
    private final ObservationJpaRepository observationRepository;
    private final SpringDataAccreditationProcessRepository processRepository;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    public DashboardDataLoader(ProgramDashboardSummaryJpaRepository summaryRepository,
                               ObservationJpaRepository observationRepository,
                               SpringDataAccreditationProcessRepository processRepository,
                               EntityManager entityManager,
                               PlatformTransactionManager transactionManager) {
        this.summaryRepository = summaryRepository;
        this.observationRepository = observationRepository;
        this.processRepository = processRepository;
        this.entityManager = entityManager;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        synchronized (LOCK) {
            try {
                transactionTemplate.execute(status -> {
                    seedSummaries();
                    seedRealProcessAndIndicators();
                    seedObservations();
                    return null;
                });
            } catch (Exception e) {
                System.err.println("Dashboard seeding skipped/failed due to concurrent execution: " + e.getMessage());
            }
        }
    }

    private void seedSummaries() {
        if (summaryRepository.count() > 0) {
            return;
        }

        // 1. Systems Engineering Summary
        ProgramDashboardSummaryEntity systemsSummary = new ProgramDashboardSummaryEntity();
        systemsSummary.setProgramId(DevSeedData.PROGRAM_INF_SIS);
        systemsSummary.setProgramName("Ingeniería de Sistemas");
        systemsSummary.setTotalIndicators(45);
        systemsSummary.setOverallProgressPercentage(68.5);
        systemsSummary.setApprovedEvidences(120);
        systemsSummary.setRejectedEvidences(15);
        systemsSummary.setPendingObservations(8);
        systemsSummary.setUpdatedAt(Instant.now());

        List<ProgramPhaseSummaryEntity> systemsPhases = new ArrayList<>();
        systemsPhases.add(createPhaseSummary(systemsSummary, 1, "Fase 1: Autoevaluación", 100.0, "COMPLETED"));
        systemsPhases.add(createPhaseSummary(systemsSummary, 2, "Fase 2: Verificación de Evidencias", 65.0, "IN_PROCESO"));
        systemsSummary.setPhases(systemsPhases);

        summaryRepository.save(systemsSummary);

        // 2. CEUB Summary
        ProgramDashboardSummaryEntity ceubSummary = new ProgramDashboardSummaryEntity();
        ceubSummary.setProgramId(DevSeedData.PROGRAM_CEUB);
        ceubSummary.setProgramName("Coordinación CEUB (demo)");
        ceubSummary.setTotalIndicators(40);
        ceubSummary.setOverallProgressPercentage(80.0);
        ceubSummary.setApprovedEvidences(100);
        ceubSummary.setRejectedEvidences(5);
        ceubSummary.setPendingObservations(2);
        ceubSummary.setUpdatedAt(Instant.now());

        List<ProgramPhaseSummaryEntity> ceubPhases = new ArrayList<>();
        ceubPhases.add(createPhaseSummary(ceubSummary, 1, "Planificación", 100.0, "COMPLETED"));
        ceubPhases.add(createPhaseSummary(ceubSummary, 2, "Ejecución", 60.0, "IN_PROCESO"));
        ceubSummary.setPhases(ceubPhases);

        summaryRepository.save(ceubSummary);

        // 3. ARCUSUR Summary
        ProgramDashboardSummaryEntity arcusurSummary = new ProgramDashboardSummaryEntity();
        arcusurSummary.setProgramId(DevSeedData.PROGRAM_ARCUSUR);
        arcusurSummary.setProgramName("Coordinación ARCU-SUR (demo)");
        arcusurSummary.setTotalIndicators(50);
        arcusurSummary.setOverallProgressPercentage(45.0);
        arcusurSummary.setApprovedEvidences(60);
        arcusurSummary.setRejectedEvidences(20);
        arcusurSummary.setPendingObservations(14);
        arcusurSummary.setUpdatedAt(Instant.now());

        List<ProgramPhaseSummaryEntity> arcusurPhases = new ArrayList<>();
        arcusurPhases.add(createPhaseSummary(arcusurSummary, 1, "Autoevaluación", 90.0, "IN_PROCESO"));
        arcusurSummary.setPhases(arcusurPhases);

        summaryRepository.save(arcusurSummary);
    }

    private ProgramPhaseSummaryEntity createPhaseSummary(ProgramDashboardSummaryEntity programSummary,
                                                        int phaseId, String name, double percentage, String status) {
        ProgramPhaseSummaryEntity phase = new ProgramPhaseSummaryEntity();
        phase.setId(UUID.randomUUID());
        phase.setProgramSummary(programSummary);
        phase.setPhaseId(phaseId);
        phase.setName(name);
        phase.setPercentage(percentage);
        phase.setStatus(status);
        return phase;
    }

    private void seedObservations() {
        if (observationRepository.count() > 0) {
            return;
        }

        // Observations for Systems Engineering
        createObservation(
                "OBS-2026-089",
                DevSeedData.PROGRAM_INF_SIS,
                "IND-102",
                "IND-3.1.2",
                "Infraestructura de Laboratorios de Computación",
                "Evidencia incompleta: falta certificado de calibración de equipos.",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(4),
                2,
                "PENDIENTE_SUBSANACION",
                "/coordinator/evidences/IND-102/subsanar"
        );

        createObservation(
                "OBS-2026-090",
                DevSeedData.PROGRAM_INF_SIS,
                "IND-101",
                "IND-1.1.1",
                "Misión y Visión del Programa Académico",
                "El documento adjunto no cuenta con la firma digital del Honorable Consejo de Carrera.",
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(2),
                1,
                "PENDIENTE_SUBSANACION",
                "/coordinator/evidences/IND-101/subsanar"
        );

        createObservation(
                "OBS-2026-091",
                DevSeedData.PROGRAM_INF_SIS,
                "IND-105",
                "IND-2.1.4",
                "Reglamento de Modalidades de Graduación",
                "La normativa citada fue derogada en la gestión 2024. Actualizar al nuevo reglamento.",
                LocalDate.now().minusDays(3),
                LocalDate.now().plusDays(10),
                2,
                "EN_REVISION_TECNICA",
                "/coordinator/evidences/IND-105/subsanar"
        );

        // Observation for CEUB
        createObservation(
                "OBS-2026-092",
                DevSeedData.PROGRAM_CEUB,
                "IND-201",
                "IND-3.1.2",
                "Infraestructura de Laboratorios de Computación",
                "Evidencia incompleta: falta certificado de calibración de equipos.",
                LocalDate.now().minusDays(7),
                LocalDate.now().plusDays(5),
                2,
                "PENDIENTE_SUBSANACION",
                "/coordinator/evidences/IND-201/subsanar"
        );
    }

    private void createObservation(String observationId, UUID programId, String indicatorId,
                                   String indicatorCode, String indicatorTitle, String description,
                                   LocalDate issueDate, LocalDate dueDate, Integer phaseId,
                                   String status, String remediationUrl) {
        ObservationEntity obs = new ObservationEntity();
        obs.setObservationId(observationId);
        obs.setProgramId(programId);
        obs.setIndicatorId(indicatorId);
        obs.setIndicatorCode(indicatorCode);
        obs.setIndicatorTitle(indicatorTitle);
        obs.setDescription(description);
        obs.setIssueDate(issueDate);
        obs.setDueDate(dueDate);
        obs.setPhaseId(phaseId);
        obs.setStatus(status);
        obs.setRemediationUrl(remediationUrl);
        observationRepository.save(obs);
    }

    private void seedRealProcessAndIndicators() {
        if (processRepository.count() > 0) {
            return;
        }

        UUID processId = DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE;

        com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity process =
                com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity.builder()
                        .id(processId)
                        .careerId(DevSeedData.PROGRAM_INF_SIS)
                        .templateId(DevSeedData.TEMPLATE_CEUB_2026)
                        .status("ACTIVE")
                        .startDate(LocalDateTime.now())
                        .phases(new ArrayList<>())
                        .build();

        process = entityManager.merge(process);

        UUID phase1Id = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");
        UUID phase2Id = UUID.fromString("550e8400-e29b-41d4-a716-446655440005");

        com.umss.sigesa.adapter.out.persistance.entity.PhaseJpaEntity phase1 =
                com.umss.sigesa.adapter.out.persistance.entity.PhaseJpaEntity.builder()
                        .id(phase1Id)
                        .name("Fase 1: Autoevaluación")
                        .order(1)
                        .process(process)
                        .build();
        entityManager.merge(phase1);

        com.umss.sigesa.adapter.out.persistance.entity.PhaseJpaEntity phase2 =
                com.umss.sigesa.adapter.out.persistance.entity.PhaseJpaEntity.builder()
                        .id(phase2Id)
                        .name("Fase 2: Verificación de Evidencias")
                        .order(2)
                        .process(process)
                        .build();
        entityManager.merge(phase2);

        UUID ind1Id = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        UUID ind2Id = UUID.fromString("550e8400-e29b-41d4-a716-446655440050");
        UUID ind3Id = UUID.fromString("550e8400-e29b-41d4-a716-446655440051");
        UUID ind4Id = UUID.fromString("550e8400-e29b-41d4-a716-446655440052");

        persistIndicator(ind1Id, DevSeedData.PROGRAM_INF_SIS, phase1Id);
        persistIndicator(ind2Id, DevSeedData.PROGRAM_INF_SIS, phase1Id);
        persistIndicator(ind3Id, DevSeedData.PROGRAM_INF_SIS, phase2Id);
        persistIndicator(ind4Id, DevSeedData.PROGRAM_INF_SIS, phase2Id);

        UUID actorId = UUID.randomUUID();
        persistStateHistory(ind1Id, IndicatorState.PENDIENTE, actorId);
        persistStateHistory(ind2Id, IndicatorState.APROBADO, actorId);
        persistStateHistory(ind3Id, IndicatorState.OBSERVADO, actorId);
        persistStateHistory(ind4Id, IndicatorState.PENDIENTE, actorId);
    }

    private void persistIndicator(UUID id, UUID programId, UUID phaseId) {
        if (entityManager.find(com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity.class, id) != null) {
            return;
        }
        com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity indicator = new com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity();
        indicator.setId(id);
        indicator.setProgramId(programId);
        indicator.setCriterionId(UUID.randomUUID());
        indicator.setPhaseId(phaseId);
        entityManager.merge(indicator);
    }

    private void persistStateHistory(UUID indicatorId, IndicatorState state, UUID actorId) {
        com.umss.sigesa.adapter.out.persistance.entity.IndicatorStateHistoryEntity history =
                new com.umss.sigesa.adapter.out.persistance.entity.IndicatorStateHistoryEntity();
        history.setId(UUID.randomUUID());
        history.setIndicatorId(indicatorId);
        history.setPreviousState(IndicatorState.PENDIENTE);
        history.setNewState(state);
        history.setActorId(actorId);
        history.setActorRole(Role.CC);
        history.setCreatedAt(LocalDateTime.now());
        entityManager.merge(history);
    }
}
