package com.umss.sigesa.application.service.assistant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantReplyOutputGuardTest {

    @Test
    @DisplayName("Redacta contraseña demo CC cuando el guard está activo")
    void redactsDemoPassword() {
        AssistantReplyOutputGuard guard = new AssistantReplyOutputGuard(true);
        String out = guard.sanitize("La contraseña es CoordDemo2026! según seed.");
        assertThat(out).doesNotContain("CoordDemo2026!");
        assertThat(out).contains("[REDACTADO POR POLÍTICA SIGESA]");
    }

    @Test
    @DisplayName("Desactivado devuelve texto sin cambios")
    void disabledPassthrough() {
        AssistantReplyOutputGuard guard = new AssistantReplyOutputGuard(false);
        assertThat(guard.sanitize("CoordDemo2026!")).isEqualTo("CoordDemo2026!");
    }
}
