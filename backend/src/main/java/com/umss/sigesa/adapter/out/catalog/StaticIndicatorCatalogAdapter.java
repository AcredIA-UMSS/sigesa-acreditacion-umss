package com.umss.sigesa.adapter.out.catalog;

import com.umss.sigesa.application.port.out.IndicatorCatalogPort;
import com.umss.sigesa.config.DevSeedData;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class StaticIndicatorCatalogAdapter implements IndicatorCatalogPort {

    private static final List<IndicatorEntry> CATALOG = List.of(
            new IndicatorEntry(
                    DevSeedData.INDICATOR_102,
                    "IND-3.1.2",
                    "Infraestructura de Laboratorios de Computación",
                    DevSeedData.PROGRAM_INF_SIS,
                    2,
                    DevSeedData.CRITERION_3_1
            ),
            new IndicatorEntry(
                    DevSeedData.INDICATOR_201,
                    "IND-2.1.1",
                    "Plan de Estudios Actualizado",
                    DevSeedData.PROGRAM_INF_SIS,
                    1,
                    DevSeedData.CRITERION_2_1
            ),
            new IndicatorEntry(
                    DevSeedData.INDICATOR_305,
                    "IND-3.5.4",
                    "Gestión de Recursos Bibliográficos",
                    DevSeedData.PROGRAM_INF_SIS,
                    2,
                    DevSeedData.CRITERION_3_5
            )
    );

    @Override
    public List<IndicatorEntry> findAll(UUID programId, Integer phaseId) {
        return CATALOG.stream()
                .filter(entry -> programId == null || entry.programId().equals(programId))
                .filter(entry -> phaseId == null || entry.phaseId() == phaseId)
                .toList();
    }

    @Override
    public Optional<IndicatorEntry> findById(UUID indicatorId) {
        return CATALOG.stream()
                .filter(entry -> entry.id().equals(indicatorId))
                .findFirst();
    }
}
