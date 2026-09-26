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

    private static final Pattern USER_CONFIRMATION_REQUEST = Pattern.compile(
            "(?i)confírm|confirmar|confirma\\s+en\\s+tu\\s+respuesta|quedó\\s+aprobado");

    private static final Pattern EXPLICIT_CANNOT_CONFIRM = Pattern.compile(
            "(?i)no puedo confirmar|no dispongo|no confirmo|no tengo");

    private final boolean enabled;

    public AssistantReplyOutputGuard(boolean enabled) {
        this.enabled = enabled;
    }

    public String sanitize(String reply) {
        return sanitize(reply, false, false);
    }

    /**
     * @param toolLookupFailed true si alguna tool de consulta terminó con success=false
     * @param userRequestedConfirmation el usuario pide confirmar un hecho/estado del sistema
     */
    public String sanitize(String reply, boolean toolLookupFailed, boolean userRequestedConfirmation) {
        if (reply == null || reply.isBlank()) {
            return reply;
        }
        String out = applyRedactions(reply);
        if (!enabled) {
            return out;
        }
        if (toolLookupFailed
                && userRequestedConfirmation
                && !EXPLICIT_CANNOT_CONFIRM.matcher(out).find()) {
            return "No puedo confirmar lo que indicas. " + out.trim();
        }
        return out;
    }

    public static boolean userMessageRequestsConfirmation(String userMessage) {
        return userMessage != null && USER_CONFIRMATION_REQUEST.matcher(userMessage).find();
    }

    private String applyRedactions(String reply) {
        if (!enabled) {
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
