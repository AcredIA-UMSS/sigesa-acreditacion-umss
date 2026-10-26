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
                    seedSemaphoreDemo();
                    return null;
                });
                transactionTemplate.execute(status -> {
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

    /**
     * Avance de demostración para el semáforo de jefatura (FSD-UC-013).
     * Algunas carreras quedan sin fila a propósito: en el panel salen en gris.
     */
    private void seedSemaphoreDemo() {
        upsertSummary(DevSeedData.PROGRAM_INF_SIS, "Ingeniería de Sistemas", 40, 92.0, 0);
        upsertSummary(DevSeedData.PROGRAM_ING_CIVIL, "Ingeniería Civil", 36, 68.0, 2);
        upsertSummary(DevSeedData.PROGRAM_ING_QUIMICA, "Ingeniería Química", 30, 42.0, 1);
        upsertSummary(DevSeedData.PROGRAM_ING_INDUSTRIAL, "Ingeniería Industrial", 28, 88.0, 1);
        upsertSummary(DevSeedData.PROGRAM_ING_ELECTRONICA, "Ingeniería Electrónica", 24, 95.0, 1);
        upsertSummary(DevSeedData.PROGRAM_ING_AMBIENTAL, "Ingeniería Ambiental", 22, 84.0, 0);
        upsertSummary(DevSeedData.PROGRAM_ING_PETROLEO, "Ingeniería de Petróleo y Gas Natural", 20, 57.0, 1);
        upsertSummary(DevSeedData.PROGRAM_ING_MINAS, "Ingeniería en Minas", 18, 31.0, 1);
        upsertSummary(DevSeedData.PROGRAM_ING_TELECOM, "Ingeniería de Telecomunicaciones", 16, 100.0, 0);
        upsertSummary(DevSeedData.PROGRAM_ARQUITECTURA, "Arquitectura", 20, 74.0, 1);
        upsertSummary(DevSeedData.PROGRAM_MEDICINA, "Medicina", 48, 46.0, 1);
        upsertSummary(DevSeedData.PROGRAM_ENFERMERIA, "Enfermería", 18, 91.0, 0);
        upsertSummary(DevSeedData.PROGRAM_DERECHO, "Derecho", 26, 63.0, 1);
        upsertSummary(DevSeedData.PROGRAM_ADM_EMPRESAS, "Administración de Empresas", 14, 90.0, 0);
        upsertSummary(DevSeedData.PROGRAM_PSICOLOGIA, "Psicología", 12, 86.0, 0);
        upsertSummary(DevSeedData.PROGRAM_BIOLOGIA, "Biología", 16, 35.0, 1);
        upsertSummary(DevSeedData.PROGRAM_ING_AGRONOMICA, "Ingeniería Agronómica", 15, 79.0, 0);
        ensureOverdueObservation();
        ensureDemoObservations();
    }

    private void ensureDemoObservations() {
        demoObservation("OBS-DEMO-CIV-1", DevSeedData.PROGRAM_ING_CIVIL, "IND-CIV-1", "IND-2.1",
                "Planos de laboratorios", "Falta el plano actualizado del laboratorio de hidráulica.", 12);
        demoObservation("OBS-DEMO-CIV-2", DevSeedData.PROGRAM_ING_CIVIL, "IND-CIV-2", "IND-2.4",
                "Convenios de pasantía", "Dos convenios vencieron y no tienen adenda.", 20);
        demoObservation("OBS-DEMO-IND-1", DevSeedData.PROGRAM_ING_INDUSTRIAL, "IND-IND-1", "IND-3.1",
                "Indicadores de empleabilidad", "La cohorte 2024 no tiene seguimiento de egresados.", 8);
        demoObservation("OBS-DEMO-ARQ-1", DevSeedData.PROGRAM_ARQUITECTURA, "IND-ARQ-1", "IND-1.3",
                "Talleres de diseño", "El inventario de mesas de dibujo está incompleto.", 15);
        demoObservation("OBS-DEMO-DER-1", DevSeedData.PROGRAM_DERECHO, "IND-DER-1", "IND-5.2",
                "Clínica jurídica", "No se adjuntó el informe del segundo semestre.", 6);
        demoObservation("OBS-DEMO-PET-1", DevSeedData.PROGRAM_ING_PETROLEO, "IND-PET-1", "IND-4.1",
                "Prácticas de campo", "Falta el seguro de los estudiantes del último viaje.", 9);
        demoObservation("OBS-DEMO-QUI-1", DevSeedData.PROGRAM_ING_QUIMICA, "IND-QUI-1", "IND-6.1",
                "Manejo de reactivos", "El inventario de reactivos no cuadra con el almacén.", -4);
        demoObservation("OBS-DEMO-MIN-1", DevSeedData.PROGRAM_ING_MINAS, "IND-MIN-1", "IND-7.2",
                "Seguridad en mina escuela", "El simulacro de evacuación está fuera de plazo.", -10);
        demoObservation("OBS-DEMO-MED-1", DevSeedData.PROGRAM_MEDICINA, "IND-MED-1", "IND-8.1",
                "Campos clínicos", "Dos hospitales no renovaron el convenio docente.", -6);
        demoObservation("OBS-DEMO-BIO-1", DevSeedData.PROGRAM_BIOLOGIA, "IND-BIO-1", "IND-3.4",
                "Colección biológica", "El registro de especímenes no está actualizado.", -2);
    }

    private void demoObservation(String id, UUID programId, String indicatorId, String code,
                                 String title, String description, int dueInDays) {
        if (observationRepository.existsById(id)) {
            return;
        }
        createObservation(
                id,
                programId,
                indicatorId,
                code,
                title,
                description,
                LocalDate.now().minusDays(15),
                LocalDate.now().plusDays(dueInDays),
                1,
                "PENDIENTE_SUBSANACION",
                "/executive/semaphore"
        );
    }

    private void upsertSummary(UUID programId, String name, int total, double progress, int pending) {
        ProgramDashboardSummaryEntity summary = summaryRepository.findById(programId)
                .orElseGet(ProgramDashboardSummaryEntity::new);
        summary.setProgramId(programId);
        summary.setProgramName(name);
        summary.setTotalIndicators(total);
        summary.setOverallProgressPercentage(progress);
        summary.setApprovedEvidences((int) Math.round(total * progress / 100.0));
        summary.setRejectedEvidences(Math.max(0, pending));
        summary.setPendingObservations(pending);
        summary.setUpdatedAt(Instant.now());
        summaryRepository.save(summary);
    }

    private void ensureOverdueObservation() {
        if (observationRepository.existsById("OBS-DEMO-VENCIDA-ELE")) {
            return;
        }
        createObservation(
                "OBS-DEMO-VENCIDA-ELE",
                DevSeedData.PROGRAM_ING_ELECTRONICA,
                "IND-ELE-01",
                "IND-4.2.1",
                "Laboratorios de electrónica",
                "Observación vencida de demostración para el semáforo rojo.",
                LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(3),
                1,
                "PENDIENTE_SUBSANACION",
                "/executive/semaphore"
        );
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

        entityManager.createNativeQuery("""
                INSERT INTO accreditation_processes (id, career_id, template_id, status, start_date, operational_mode)
                VALUES (?, ?, ?, ?, ?, ?)
                """)
                .setParameter(1, processId)
                .setParameter(2, DevSeedData.PROGRAM_INF_SIS)
                .setParameter(3, DevSeedData.TEMPLATE_CEUB_2026)
                .setParameter(4, "ACTIVE")
                .setParameter(5, LocalDateTime.now())
                .setParameter(6, "ACTIVE")
                .executeUpdate();
    }

    private void persistIndicatorUnused(UUID id, UUID programId, UUID phaseId) {
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
