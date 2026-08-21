package com.umss.sigesa.application.service.normative;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Reduce preguntas naturales a términos útiles para FTS/LIKE del índice normativo.
 */
public final class NormativeSearchQueryNormalizer {

    private static final Pattern QUESTION_PREFIX = Pattern.compile(
            "(?is)^\\s*(qu[eé]|que)\\s+(dice|dices|significa|es|indica|menciona)\\s+"
                    + "(la\\s+)?(normativa\\s+)?(sobre|de|del|acerca\\s+de)?\\s*");

    private static final Pattern SEARCH_PREFIX = Pattern.compile(
            "(?is)^\\s*(buscar|busca|consultar|consulta|informaci[oó]n)\\s+(en\\s+)?(la\\s+)?"
                    + "(normativa\\s+)?(sobre|de|del|acerca\\s+de)?\\s*");

    private static final Set<String> STOPWORDS = Set.of(
            "que", "qué", "dice", "dices", "significa", "es", "la", "el", "los", "las", "un", "una",
            "sobre", "de", "del", "en", "y", "o", "a", "con", "para", "por", "al", "normativa",
            "documentacion", "documentación", "buscar", "busca", "consultar", "consulta", "informacion",
            "información", "acerca", "como", "cómo", "cual", "cuál", "cuales", "cuáles");

    private NormativeSearchQueryNormalizer() {
    }

    public static String condensedForSearch(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return "";
        }
        String sanitized = sanitize(rawQuery);
        String stripped = QUESTION_PREFIX.matcher(sanitized).replaceAll("").trim();
        stripped = SEARCH_PREFIX.matcher(stripped).replaceAll("").trim();
        if (stripped.isBlank()) {
            stripped = sanitized;
        }

        String[] tokens = stripped.split("\\s+");
        List<String> kept = new ArrayList<>();
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            String lower = token.toLowerCase(Locale.ROOT);
            if (token.length() >= 2 && !STOPWORDS.contains(lower)) {
                kept.add(token);
            }
        }
        if (kept.isEmpty()) {
            return stripped;
        }
        return String.join(" ", kept);
    }

    public static List<String> significantTerms(String rawQuery) {
        String condensed = condensedForSearch(rawQuery);
        if (condensed.isBlank()) {
            return List.of();
        }
        List<String> terms = new ArrayList<>();
        for (String token : condensed.split("\\s+")) {
            if (token.length() >= 3) {
                terms.add(token);
            }
        }
        if (terms.isEmpty()) {
            terms.add(condensed);
        }
        return terms;
    }

    public static String sanitize(String query) {
        return query.trim()
                .replaceAll("[^\\p{L}\\p{N}\\s\\-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
