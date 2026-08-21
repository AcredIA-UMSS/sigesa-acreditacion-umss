package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.adapter.in.web.dto.ChatMessageDto;
import com.umss.sigesa.domain.exception.AssistantInvalidInputException;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Valida y rechaza entradas del chat del asistente con patrones de inyección SQL,
 * XSS y caracteres de control. Defensa en profundidad: las tools usan casos de uso
 * tipados, pero el mensaje del usuario no debe atravesar el pipeline sin inspección.
 */
public class AssistantChatInputValidator {

    public static final int MAX_MESSAGE_LENGTH = 4_000;
    public static final int MAX_HISTORY_SIZE = 30;

    private static final Set<String> ALLOWED_ROLES = Set.of("user", "assistant", "system", "tool");

    private static final List<Pattern> BLOCKED_PATTERNS = List.of(
            Pattern.compile("(?i)\\bselect\\b.+\\bfrom\\b"),
            Pattern.compile("(?i)\\bunion\\s+select\\b"),
            Pattern.compile("(?i)\\bdrop\\s+table\\b"),
            Pattern.compile("(?i)\\bdelete\\s+from\\b"),
            Pattern.compile("(?i)\\binsert\\s+into\\b"),
            Pattern.compile("(?i)\\bupdate\\s+\\w+\\s+set\\b"),
            Pattern.compile("(?i);\\s*--"),
            Pattern.compile("(?i)'\\s*or\\s+'1'\\s*=\\s*'1"),
            Pattern.compile("(?i)/\\*|\\*/"),
            Pattern.compile("(?i)\\b(exec|execute|xp_)\\b"),
            Pattern.compile("(?i)<\\s*script"),
            Pattern.compile("(?i)javascript\\s*:"),
            Pattern.compile("(?i)\\bon\\w+\\s*="),
            Pattern.compile("\\u0000"));

    public void validateMessage(String message) {
        validateText(message, "message");
    }

    public void validateHistory(List<ChatMessageDto> history) {
        if (history == null || history.isEmpty()) {
            return;
        }
        if (history.size() > MAX_HISTORY_SIZE) {
            throw new AssistantInvalidInputException(
                    "El historial excede el máximo permitido (" + MAX_HISTORY_SIZE + " mensajes).");
        }
        for (int i = 0; i < history.size(); i++) {
            ChatMessageDto item = history.get(i);
            if (item.role() == null || !ALLOWED_ROLES.contains(item.role().trim().toLowerCase())) {
                throw new AssistantInvalidInputException(
                        "Rol inválido en historial (índice " + i + ").");
            }
            validateText(item.content(), "history[" + i + "].content");
        }
    }

    private void validateText(String text, String field) {
        if (text == null || text.isBlank()) {
            throw new AssistantInvalidInputException("El campo '" + field + "' no puede estar vacío.");
        }
        if (text.length() > MAX_MESSAGE_LENGTH) {
            throw new AssistantInvalidInputException(
                    "El campo '" + field + "' excede " + MAX_MESSAGE_LENGTH + " caracteres.");
        }
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\n' || ch == '\r' || ch == '\t') {
                continue;
            }
            if (Character.isISOControl(ch)) {
                throw new AssistantInvalidInputException(
                        "El campo '" + field + "' contiene caracteres de control no permitidos.");
            }
        }
        for (Pattern pattern : BLOCKED_PATTERNS) {
            if (pattern.matcher(text).find()) {
                throw new AssistantInvalidInputException(
                        "El mensaje contiene contenido no permitido por políticas de seguridad.");
            }
        }
    }
}
