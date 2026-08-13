package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.IndicatorJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Seed de indicador PENDIENTE para demo UC-004.
 * El usuario CC y su assignment a {@link DevSeedData#PROGRAM_INF_SIS} los crea {@link AuthDataLoader}.
 */
@Component
@Order(2)
public class EvidenceDataLoader implements ApplicationRunner {

    public static final UUID SEED_PROGRAM_ID = DevSeedData.PROGRAM_INF_SIS;
    public static final UUID SEED_DIMENSION_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    public static final UUID SEED_CRITERION_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    public static final UUID SEED_INDICATOR_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
    public static final UUID SEED_PHASE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");

    private final IndicatorJpaRepository indicatorRepository;

    public EvidenceDataLoader(IndicatorJpaRepository indicatorRepository) {
        this.indicatorRepository = indicatorRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedIndicator();
    }

    private void seedIndicator() {
        if (indicatorRepository.existsById(SEED_INDICATOR_ID)) {
            return;
        }
        IndicatorEntity indicator = new IndicatorEntity();
        indicator.setId(SEED_INDICATOR_ID);
        indicator.setProgramId(SEED_PROGRAM_ID);
        indicator.setCriterionId(SEED_CRITERION_ID);
        indicator.setPhaseId(SEED_PHASE_ID);
        indicatorRepository.save(indicator);
    }
}
