package com.umss.sigesa.application.service.assistant;

import java.util.regex.Pattern;

/**
 * Detecta preguntas claramente fuera del dominio SIGESA (escenario 3) antes de invocar al LLM.
 */
public final class AssistantOutOfScopeDetector {

    private static final Pattern OUT_OF_SCOPE_PATTERN = Pattern.compile(
            "(?is).*(presupuesto|finanzas|financier[oa]s?|n[oó]mina|salarios?|"
                    + "balance\\s+general|estado\\s+financiero|clima|tiempo\\s+meteorol[oó]gico|"
                    + "noticias\\s+(de\\s+)?hoy|resultado\\s+de\\s+f[uú]tbol).*");

    private AssistantOutOfScopeDetector() {
    }

    public static boolean isOutOfScope(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return false;
        }
        return OUT_OF_SCOPE_PATTERN.matcher(userMessage.trim()).matches();
    }
}
