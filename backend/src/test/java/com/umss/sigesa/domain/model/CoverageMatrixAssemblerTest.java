package com.umss.sigesa.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CoverageMatrixAssemblerTest {

    @Test
    void assemble_groupsTreeAndComputesCoverage() {
        UUID dim = UUID.randomUUID();
        UUID component = UUID.randomUUID();
        UUID criterion = UUID.randomUUID();
        UUID approved = UUID.randomUUID();
        UUID observed = UUID.randomUUID();
        UUID pending = UUID.randomUUID();

        CoverageMatrix matrix = CoverageMatrixAssembler.assemble(
                UUID.randomUUID(),
                "Ingeniería de Sistemas",
                List.of(
                        row(dim, "N1", 1, component, "N2", 1, criterion, "Criterio 1.1", 1,
                                approved, "IND-1", IndicatorState.APROBADO.name(), 2, 1),
                        row(dim, "N1", 1, component, "N2", 1, criterion, "Criterio 1.1", 1,
                                observed, "IND-2", IndicatorState.OBSERVADO.name(), 0, 2),
                        row(dim, "N1", 1, component, "N2", 1, criterion, "Criterio 1.1", 1,
                                pending, "IND-3", IndicatorState.PENDIENTE.name(), 1, 3)
                ));

        assertThat(matrix.totals().indicators()).isEqualTo(3);
        assertThat(matrix.totals().withEvidence()).isEqualTo(2);
        assertThat(matrix.totals().approved()).isEqualTo(1);
        assertThat(matrix.totals().observed()).isEqualTo(1);
        assertThat(matrix.totals().other()).isEqualTo(1);
        assertThat(matrix.totals().coveragePercent()).isEqualTo(66.67);
        assertThat(matrix.dimensions()).hasSize(1);
        CoverageMatrix.CriterionNode criterionNode = matrix.dimensions().getFirst()
                .components().getFirst()
                .criteria().getFirst();
        assertThat(criterionNode.name()).isEqualTo("Criterio 1.1");
        assertThat(criterionNode.indicators()).extracting(CoverageMatrix.IndicatorRow::code)
                .containsExactly("IND-1", "IND-2", "IND-3");
        assertThat(criterionNode.indicators().getFirst().evidenceCount()).isEqualTo(2);
    }

    @Test
    void assemble_emptyTreeHasZeroTotals() {
        CoverageMatrix matrix = CoverageMatrixAssembler.assemble(UUID.randomUUID(), "Carrera", List.of());
        assertThat(matrix.totals().indicators()).isZero();
        assertThat(matrix.totals().coveragePercent()).isZero();
        assertThat(matrix.dimensions()).isEmpty();
    }

    private static CoverageIndicatorInput row(
            UUID dimId, String dimName, int dimOrder,
            UUID compId, String compName, int compOrder,
            UUID critId, String critName, int critOrder,
            UUID indId, String code, String status, int evidence, int indOrder) {
        return new CoverageIndicatorInput(
                dimId, dimName, dimOrder,
                compId, compName, compOrder,
                critId, critName, critOrder,
                indId, code, "desc", status, evidence, indOrder);
    }
}
