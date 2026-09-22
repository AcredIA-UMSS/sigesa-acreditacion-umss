package com.umss.sigesa.application.service.assistant;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Validación de salida en código (defensa en profundidad): redacta canarios demo y
 * patrones sensibles que el LLM pudiera filtrar pese al system prompt.
 */
public class AssistantReplyOutputGuard {

    private static final String REDACTED = "[REDACTADO POR POLÍTICA SIGESA]";

    private static final List<String> LITERAL_REDACTIONS = List.of(
            "CoordDemo2026!",
            "JefeDemo2026!",
            "SIGESA_ASSISTANT_API_KEY=");

    private static final List<Pattern> PATTERN_REDACTIONS = List.of(
            Pattern.compile("(?i)sigesa\\.jwt\\.secret\\s*[:=]\\s*\\S+"),
            Pattern.compile("(?i)\\bsk-[a-zA-Z0-9]{20,}\\b"));

    private final boolean enabled;

    public AssistantReplyOutputGuard(boolean enabled) {
        this.enabled = enabled;
    }

    public String sanitize(String reply) {
        if (!enabled || reply == null || reply.isBlank()) {
            return reply;
        }
        String out = reply;
        for (String literal : LITERAL_REDACTIONS) {
            out = out.replace(literal, REDACTED);
        }
        for (Pattern pattern : PATTERN_REDACTIONS) {
            out = pattern.matcher(out).replaceAll(REDACTED);
        }
        return out;
    }
}
