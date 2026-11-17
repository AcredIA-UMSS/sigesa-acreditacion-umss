package com.umss.sigesa.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CoverageMatrixAssembler {

    private CoverageMatrixAssembler() {
    }

    public static CoverageMatrix assemble(
            UUID processId,
            String careerName,
            List<CoverageIndicatorInput> rows) {
        List<CoverageIndicatorInput> sorted = rows.stream()
                .sorted(Comparator
                        .comparingInt(CoverageIndicatorInput::dimensionOrder)
                        .thenComparingInt(CoverageIndicatorInput::componentOrder)
                        .thenComparingInt(CoverageIndicatorInput::criterionOrder)
                        .thenComparingInt(CoverageIndicatorInput::indicatorOrder))
                .toList();

        int indicators = sorted.size();
        int withEvidence = 0;
        int approved = 0;
        int observed = 0;
        for (CoverageIndicatorInput row : sorted) {
            if (row.evidenceCount() > 0) {
                withEvidence++;
            }
            if (IndicatorState.APROBADO.name().equals(row.status())) {
                approved++;
            } else if (IndicatorState.OBSERVADO.name().equals(row.status())) {
                observed++;
            }
        }
        int other = indicators - approved - observed;
        double coverage = indicators == 0 ? 0.0 : Math.round((withEvidence * 10000.0) / indicators) / 100.0;

        Map<UUID, List<CoverageIndicatorInput>> byDimension = new LinkedHashMap<>();
        for (CoverageIndicatorInput row : sorted) {
            byDimension.computeIfAbsent(row.dimensionId(), ignored -> new ArrayList<>()).add(row);
        }

        List<CoverageMatrix.DimensionNode> dimensions = new ArrayList<>();
        for (List<CoverageIndicatorInput> dimRows : byDimension.values()) {
            CoverageIndicatorInput first = dimRows.getFirst();
            dimensions.add(new CoverageMatrix.DimensionNode(
                    first.dimensionId(),
                    first.dimensionName(),
                    assembleComponents(dimRows)));
        }

        return new CoverageMatrix(
                processId,
                careerName,
                Instant.now(),
                new CoverageMatrix.Totals(indicators, withEvidence, approved, observed, other, coverage),
                dimensions);
    }

    private static List<CoverageMatrix.ComponentNode> assembleComponents(List<CoverageIndicatorInput> dimRows) {
        Map<UUID, List<CoverageIndicatorInput>> byComponent = new LinkedHashMap<>();
        for (CoverageIndicatorInput row : dimRows) {
            byComponent.computeIfAbsent(row.componentId(), ignored -> new ArrayList<>()).add(row);
        }
        List<CoverageMatrix.ComponentNode> components = new ArrayList<>();
        for (List<CoverageIndicatorInput> compRows : byComponent.values()) {
            CoverageIndicatorInput first = compRows.getFirst();
            components.add(new CoverageMatrix.ComponentNode(
                    first.componentId(),
                    first.componentName(),
                    assembleCriteria(compRows)));
        }
        return components;
    }

    private static List<CoverageMatrix.CriterionNode> assembleCriteria(List<CoverageIndicatorInput> compRows) {
        Map<UUID, List<CoverageIndicatorInput>> byCriterion = new LinkedHashMap<>();
        for (CoverageIndicatorInput row : compRows) {
            byCriterion.computeIfAbsent(row.criterionId(), ignored -> new ArrayList<>()).add(row);
        }
        List<CoverageMatrix.CriterionNode> criteria = new ArrayList<>();
        for (List<CoverageIndicatorInput> critRows : byCriterion.values()) {
            CoverageIndicatorInput first = critRows.getFirst();
            List<CoverageMatrix.IndicatorRow> indicators = critRows.stream()
                    .map(r -> new CoverageMatrix.IndicatorRow(
                            r.indicatorId(), r.code(), r.description(), r.status(), r.evidenceCount()))
                    .toList();
            criteria.add(new CoverageMatrix.CriterionNode(first.criterionId(), first.criterionName(), indicators));
        }
        return criteria;
    }
}
