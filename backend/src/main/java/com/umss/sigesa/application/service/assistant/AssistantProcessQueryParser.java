package com.umss.sigesa.application.service.assistant;

import java.util.Locale;
import java.util.regex.Pattern;

final class AssistantProcessQueryParser {

    private static final Pattern ARCU_PATTERN = Pattern.compile(
            "(?i)\\b(arcu-?sur|arcusur|arcu\\s*sur)\\b");
    private static final Pattern CEUB_PATTERN = Pattern.compile("(?i)\\bceub\\b");

    record ParsedProcessQuery(String careerQuery, String templateType) {
    }

    private AssistantProcessQueryParser() {
    }

    static ParsedProcessQuery parse(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return new ParsedProcessQuery(null, null);
        }

        String working = rawQuery.trim();
        String templateType = null;

        if (ARCU_PATTERN.matcher(working).find()) {
            templateType = "ARCU-SUR";
            working = ARCU_PATTERN.matcher(working).replaceAll(" ").trim();
        } else if (CEUB_PATTERN.matcher(working).find()) {
            templateType = "CEUB";
            working = CEUB_PATTERN.matcher(working).replaceAll(" ").trim();
        }

        working = working.replaceAll("\\s+", " ").trim();
        if (working.isEmpty()) {
            working = null;
        }

        return new ParsedProcessQuery(working, templateType);
    }

    static String normalizeTemplateType(String templateType) {
        if (templateType == null || templateType.isBlank()) {
            return null;
        }
        String normalized = templateType.trim().toUpperCase(Locale.ROOT);
        if ("ARCUSUR".equals(normalized) || "ARCU SUR".equals(normalized)) {
            return "ARCU-SUR";
        }
        return normalized;
    }
}
