package com.umss.sigesa.application.service.normative;

import com.umss.sigesa.application.model.normative.NormativeSearchPlan;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NormativeQueryPlannerTest {

    @Test
    void noisyQuery_keepsDomainTerms_andDiscardsGeneric() {
        NormativeSearchPlan plan = NormativeQueryPlanner.plan(
                "cronograma pares visita normativa procedimiento",
                null);

        assertThat(plan.selectedTerms()).contains("pares", "visita", "cronograma");
        assertThat(plan.selectedTerms()).doesNotContain("normativa", "procedimiento");
        assertThat(plan.discardedTerms()).contains("procedimiento");
        assertThat(plan.templateTypeHint()).isEqualTo("ARCU-SUR");
        assertThat(plan.strategy()).isEqualTo("WEIGHTED_SYNONYM");
    }

    @Test
    void synonymQuery_mapsEvaluadoresToPares() {
        NormativeSearchPlan plan = NormativeQueryPlanner.plan(
                "¿Quién arma el equipo de evaluadores externos?",
                null);

        assertThat(plan.selectedTerms()).contains("pares");
        assertThat(plan.expandedTerms()).anyMatch(term -> term.toLowerCase().contains("par"));
    }

    @Test
    void requestedTemplateType_winsOverHint() {
        NormativeSearchPlan plan = NormativeQueryPlanner.plan("pares ARCU-SUR", "CEUB");
        assertThat(plan.templateTypeHint()).isEqualTo("CEUB");
    }
}
