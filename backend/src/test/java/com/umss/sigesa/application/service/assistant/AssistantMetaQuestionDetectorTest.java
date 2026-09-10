package com.umss.sigesa.application.service.assistant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantMetaQuestionDetectorTest {

    @Test
    void detectsModelAndIdentityQuestions() {
        assertThat(AssistantMetaQuestionDetector.isMetaQuestion(
                "Hola sobrino que modelo estas usando y mas info acerca de vos")).isTrue();
        assertThat(AssistantMetaQuestionDetector.isMetaQuestion("¿Quién eres?")).isTrue();
        assertThat(AssistantMetaQuestionDetector.isMetaQuestion("¿Qué puedes hacer?")).isTrue();
        assertThat(AssistantMetaQuestionDetector.isMetaQuestion(
                "listame las funciones que puedo hacer como usuario")).isTrue();
    }

    @Test
    void ignoresOperationalQuestions() {
        assertThat(AssistantMetaQuestionDetector.isMetaQuestion(
                "Lista las fases de Ingeniería de Sistemas CEUB")).isFalse();
        assertThat(AssistantMetaQuestionDetector.isMetaQuestion(null)).isFalse();
        assertThat(AssistantMetaQuestionDetector.isMetaQuestion("   ")).isFalse();
    }
}
