package com.umss.sigesa.application.service.assistant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantOutOfScopeDetectorTest {

    @Test
    void detectsBudgetQuestion() {
        assertThat(AssistantOutOfScopeDetector.isOutOfScope(
                "¿Cuál es el presupuesto de la universidad para 2027?")).isTrue();
    }

    @Test
    void allowsAccreditationQuestions() {
        assertThat(AssistantOutOfScopeDetector.isOutOfScope(
                "Lista las fases de Ingeniería de Sistemas CEUB")).isFalse();
        assertThat(AssistantOutOfScopeDetector.isOutOfScope(
                "¿Qué etapas tiene el proceso activo de Ingeniería de Sistemas CEUB?")).isFalse();
    }
}
