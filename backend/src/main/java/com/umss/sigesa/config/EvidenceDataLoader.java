package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.IndicatorJpaRepository;
import com.umss.sigesa.adapter.out.persistance.IndicatorStateHistoryJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorStateHistoryEntity;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Role;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Seed de indicadores PENDIENTE con etiquetas para demo UC-004 (selects CC).
 * El usuario CC y su assignment a {@link DevSeedData#PROGRAM_INF_SIS} los crea {@link AuthDataLoader}.
 */
@Component
@Order(2)
public class EvidenceDataLoader implements ApplicationRunner {

    public static final UUID SEED_PROGRAM_ID = DevSeedData.PROGRAM_INF_SIS;
    public static final UUID SEED_PHASE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");

    public static final UUID SEED_CRITERION_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    public static final UUID SEED_INDICATOR_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");

    public static final UUID SEED_CRITERION_2_ID = UUID.fromString("660e8400-e29b-41d4-a716-446655440011");
    public static final UUID SEED_INDICATOR_2_ID = UUID.fromString("660e8400-e29b-41d4-a716-446655440010");

    public static final UUID SEED_CRITERION_3_ID = UUID.fromString("660e8400-e29b-41d4-a716-446655440013");
    public static final UUID SEED_INDICATOR_3_ID = UUID.fromString("660e8400-e29b-41d4-a716-446655440012");

    private static final UUID SEED_SYSTEM_ACTOR = UUID.fromString("00000000-0000-4000-8000-000000000001");

    private final IndicatorJpaRepository indicatorRepository;
    private final IndicatorStateHistoryJpaRepository historyRepository;

    public EvidenceDataLoader(IndicatorJpaRepository indicatorRepository,
                              IndicatorStateHistoryJpaRepository historyRepository) {
        this.indicatorRepository = indicatorRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        upsertIndicator(
                SEED_INDICATOR_ID,
                SEED_CRITERION_ID,
                "IND-01",
                "Plan de estudios vigente",
                "CRIT-01",
                "Diseño curricular");
        upsertIndicator(
                SEED_INDICATOR_2_ID,
                SEED_CRITERION_2_ID,
                "IND-02",
                "Sílabos de asignaturas troncales",
                "CRIT-02",
                "Gestión académica");
        upsertIndicator(
                SEED_INDICATOR_3_ID,
                SEED_CRITERION_3_ID,
                "IND-03",
                "Convenios de práctica preprofesional",
                "CRIT-03",
                "Vinculación con el medio");
    }

    private void upsertIndicator(UUID indicatorId,
                                 UUID criterionId,
                                 String code,
                                 String title,
                                 String criterionCode,
                                 String criterionTitle) {
        IndicatorEntity indicator = indicatorRepository.findById(indicatorId).orElseGet(IndicatorEntity::new);
        indicator.setId(indicatorId);
        indicator.setProgramId(SEED_PROGRAM_ID);
        indicator.setCriterionId(criterionId);
        indicator.setPhaseId(SEED_PHASE_ID);
        indicator.setCode(code);
        indicator.setTitle(title);
        indicator.setCriterionCode(criterionCode);
        indicator.setCriterionTitle(criterionTitle);
        indicatorRepository.save(indicator);
        ensurePendingHistory(indicatorId);
    }

    private void ensurePendingHistory(UUID indicatorId) {
        if (historyRepository.findTopByIndicatorIdOrderByCreatedAtDesc(indicatorId).isPresent()) {
            return;
        }
        IndicatorStateHistoryEntity history = new IndicatorStateHistoryEntity();
        history.setId(UUID.randomUUID());
        history.setIndicatorId(indicatorId);
        history.setPreviousState(IndicatorState.PENDIENTE);
        history.setNewState(IndicatorState.PENDIENTE);
        history.setActorId(SEED_SYSTEM_ACTOR);
        history.setActorRole(Role.JD);
        history.setCreatedAt(LocalDateTime.now());
        historyRepository.save(history);
    }
}
