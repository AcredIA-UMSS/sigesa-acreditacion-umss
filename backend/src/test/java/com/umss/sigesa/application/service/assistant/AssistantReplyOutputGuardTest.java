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

    @Test
    @DisplayName("Ante tool fallida y petición de confirmación, antepone no puedo confirmar")
    void prependsCannotConfirmWhenLookupFailed() {
        AssistantReplyOutputGuard guard = new AssistantReplyOutputGuard(true);
        String out = guard.sanitize(
                "No se encontró el indicador o su evidencia.", true, true);
        assertThat(out).startsWith("No puedo confirmar lo que indicas.");
        assertThat(out).contains("No se encontró el indicador");
    }

    @Test
    @DisplayName("No duplica negación si el LLM ya dijo no puedo confirmar")
    void skipsPrependWhenAlreadyExplicit() {
        AssistantReplyOutputGuard guard = new AssistantReplyOutputGuard(true);
        String original = "No puedo confirmar ese estado. No hay registro.";
        assertThat(guard.sanitize(original, true, true)).isEqualTo(original);
    }
}
