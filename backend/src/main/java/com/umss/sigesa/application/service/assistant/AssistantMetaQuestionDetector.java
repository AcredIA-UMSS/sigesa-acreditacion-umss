package com.umss.sigesa.application.service.assistant;

import java.util.regex.Pattern;

/**
 * Detecta preguntas sobre identidad o capacidades del asistente (sin invocar tools ni LLM).
 */
public final class AssistantMetaQuestionDetector {

    private static final Pattern META_QUESTION_PATTERN = Pattern.compile(
            "(?is).*(qu[eé]\\s+modelo|modelo\\s+(usas|utilizas|est[aá]s\\s+usando)|"
                    + "qui[eé]n\\s+(eres|soy|sos)|sobre\\s+(ti|vos|usted)|acerca\\s+de\\s+(ti|vos|usted)|"
                    + "qu[eé]\\s+(puedes|sabes)\\s+hacer|tus?\\s+capacidades|pres[eé]ntate|identif[ií]cate|"
                    + "list(a|ame)?\\s+(las\\s+)?funciones|funciones\\s+que\\s+(puedo|pod[eé])\\s+hacer|"
                    + "qu[eé]\\s+funciones\\s+(puedo|tengo|hay)|"
                    + "asistente\\s+virtual|c[oó]mo\\s+te\\s+llamas|cu[aá]l\\s+es\\s+tu\\s+nombre|"
                    + "m[aá]s\\s+info(rmaci[oó]n)?\\s+(acerca\\s+de\\s+)?(ti|vos|usted)).*");

    private AssistantMetaQuestionDetector() {
    }

    public static boolean isMetaQuestion(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return false;
        }
        return META_QUESTION_PATTERN.matcher(userMessage.trim()).matches();
    }
}
