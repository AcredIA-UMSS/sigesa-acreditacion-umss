package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.IndicatorStateHistoryJpaRepository;
import com.umss.sigesa.adapter.out.persistance.ObservationJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorStateHistoryEntity;
import com.umss.sigesa.adapter.out.persistance.entity.ObservationEntity;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Observaciones y estados de indicadores de demostración para dashboard y workflow local.
 */
@Component
@Profile("!prod")
@Order(250)
public class ObservationDevDataLoader implements ApplicationRunner {

    private final ObservationJpaRepository observationRepository;
    private final IndicatorStateHistoryJpaRepository stateHistoryRepository;

    public ObservationDevDataLoader(ObservationJpaRepository observationRepository,
                                    IndicatorStateHistoryJpaRepository stateHistoryRepository) {
        this.observationRepository = observationRepository;
        this.stateHistoryRepository = stateHistoryRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedObservations();
        seedIndicatorStates();
    }

    private void seedObservations() {
        seedObservation(
                "OBS-DEMO-001",
                DevSeedData.INDICATOR_102,
                "IND-3.1.2",
                "Infraestructura de Laboratorios de Computación",
                "Actualizar inventario de equipos de laboratorio con evidencia fotográfica.",
                2,
                "PENDING_REMEDIATION",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(9)
        );

        seedObservation(
                "OBS-DEMO-002",
                DevSeedData.INDICATOR_201,
                "IND-2.1.1",
                "Plan de Estudios Actualizado",
                "Adjuntar resolución vigente del plan de estudios firmada por Consejo Directivo.",
                1,
                "PENDING_SUBSANACION",
                LocalDate.now().minusDays(12),
                LocalDate.now().plusDays(2)
        );

        seedObservation(
                "OBS-DEMO-003",
                DevSeedData.INDICATOR_305,
                "IND-3.5.4",
                "Gestión de Recursos Bibliográficos",
                "Documentación aprobada tras revisión técnica.",
                2,
                "APROBADO",
                LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(6)
        );

        seedObservation(
                "OBS-DEMO-004",
                DevSeedData.INDICATOR_102,
                "IND-3.1.2",
                "Infraestructura de Laboratorios de Computación",
                "Evidencia en revisión por el técnico DUEA.",
                2,
                "EN_REVISION_TECNICA",
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(12)
        );
    }

    private void seedObservation(String observationId,
                                 UUID indicatorId,
                                 String indicatorCode,
                                 String indicatorTitle,
                                 String description,
                                 int phaseId,
                                 String status,
                                 LocalDate issueDate,
                                 LocalDate dueDate) {
        if (observationRepository.existsById(observationId)) {
            return;
        }

        ObservationEntity entity = new ObservationEntity();
        entity.setObservationId(observationId);
        entity.setProgramId(DevSeedData.PROGRAM_INF_SIS);
        entity.setIndicatorId(indicatorId.toString());
        entity.setIndicatorCode(indicatorCode);
        entity.setIndicatorTitle(indicatorTitle);
        entity.setDescription(description);
        entity.setIssueDate(issueDate);
        entity.setDueDate(dueDate);
        entity.setPhaseId(phaseId);
        entity.setStatus(status);
        entity.setRemediationUrl("/evidencias/" + indicatorId + "/subsanar");
        observationRepository.save(entity);
    }

    private void seedIndicatorStates() {
        seedState(DevSeedData.INDICATOR_102, "PENDIENTE", "SUBIDO");
        seedState(DevSeedData.INDICATOR_201, "PENDIENTE", "SUBIDO");
        seedState(DevSeedData.INDICATOR_305, "PENDIENTE", "APROBADO");
    }

    private void seedState(UUID indicatorId, String previousState, String newState) {
        if (stateHistoryRepository.findTopByIndicatorIdOrderByCreatedAtDesc(indicatorId).isPresent()) {
            return;
        }

        IndicatorStateHistoryEntity entity = new IndicatorStateHistoryEntity();
        entity.setId(UUID.randomUUID());
        entity.setIndicatorId(indicatorId);
        entity.setPreviousState(previousState);
        entity.setNewState(newState);
        entity.setActorId(UUID.randomUUID());
        entity.setRole("CC");
        entity.setCreatedAt(LocalDateTime.now().minusDays(1));
        stateHistoryRepository.save(entity);
    }
}
