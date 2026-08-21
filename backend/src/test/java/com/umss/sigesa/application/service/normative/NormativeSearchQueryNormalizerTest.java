package com.umss.sigesa.application.service.normative;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NormativeSearchQueryNormalizerTest {

    @Test
    void condensed_stripsNormativeQuestionPrefix() {
        assertThat(NormativeSearchQueryNormalizer.condensedForSearch(
                "¿Qué dice la normativa sobre matriz de evidencias CEUB?"))
                .isEqualTo("matriz evidencias CEUB");
    }

    @Test
    void condensed_keepsArcuSurTerms() {
        assertThat(NormativeSearchQueryNormalizer.condensedForSearch(
                "validación de criterios ARCU-SUR"))
                .contains("validación", "criterios", "ARCU-SUR");
    }

    @Test
    void significantTerms_returnsSearchableTokens() {
        assertThat(NormativeSearchQueryNormalizer.significantTerms(
                "¿Qué dice la normativa sobre matriz de evidencias CEUB?"))
                .containsExactly("matriz", "evidencias", "CEUB");
    }
}
