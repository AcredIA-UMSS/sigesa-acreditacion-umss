package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.AppUserJpaRepository;
import com.umss.sigesa.adapter.out.persistance.IndicatorStateHistoryJpaRepository;
import com.umss.sigesa.adapter.out.persistance.ObservationJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AppUserEntity;
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
 * Observaciones y estados de indicadores de demostración para el dashboard y el workflow local.
 */
@Component
@Profile("!prod")
@Order(270) // Runs after EvidenceDevDataLoader (Order 260) to ensure foreign keys are satisfied
public class ObservationDevDataLoader implements ApplicationRunner {

    private final ObservationJpaRepository observationRepository;
    private final IndicatorStateHistoryJpaRepository stateHistoryRepository;
    private final AppUserJpaRepository userRepository;

    public ObservationDevDataLoader(ObservationJpaRepository observationRepository,
                                    IndicatorStateHistoryJpaRepository stateHistoryRepository,
                                    AppUserJpaRepository userRepository) {
        this.observationRepository = observationRepository;
        this.stateHistoryRepository = stateHistoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedObservations();
        seedIndicatorStates();
    }

    private void seedObservations() {
        UUID tdUserId = userRepository.findByEmail(AuthDataLoader.SEED_TD_EMAIL)
                .map(AppUserEntity::getId)
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000088"));

        seedObservation(
                "OBS-DEMO-001",
                DevSeedData.INDICATOR_102,
                "IND-3.1.2",
                "Infraestructura de Laboratorios de Computación",
                "Actualizar inventario de equipos de laboratorio con evidencia fotográfica.",
                2,
                "PENDING_REMEDIATION",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(9),
                DevSeedData.EVIDENCE_LABS_V1,
                tdUserId
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
                LocalDate.now().plusDays(2),
                DevSeedData.EVIDENCE_PLAN_V1,
                tdUserId
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
                LocalDate.now().minusDays(6),
                DevSeedData.EVIDENCE_BIBLIO_V1,
                tdUserId
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
                LocalDate.now().plusDays(12),
                DevSeedData.EVIDENCE_LABS_V1,
                tdUserId
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
                                 LocalDate dueDate,
                                 UUID evidenceVersionId,
                                 UUID observerId) {
        UUID id = UUID.nameUUIDFromBytes(observationId.getBytes());
        if (observationRepository.existsById(id)) {
            return;
        }

        ObservationEntity entity = new ObservationEntity();
        entity.setId(id);
        entity.setEvidenceVersionId(evidenceVersionId);
        entity.setObserverId(observerId);
        entity.setRoleCode("TD");
        entity.setObservations(description);
        entity.setCreatedAt(issueDate.atStartOfDay());
        
        entity.setProgramId(DevSeedData.PROGRAM_INF_SIS);
        entity.setIndicatorId(indicatorId.toString());
        entity.setIndicatorCode(indicatorCode);
        entity.setIndicatorTitle(indicatorTitle);
        entity.setDueDate(dueDate);
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
