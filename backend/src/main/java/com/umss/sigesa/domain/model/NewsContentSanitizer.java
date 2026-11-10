package com.umss.sigesa.domain.model;

import java.util.regex.Pattern;

/**
 * Quita HTML crudo. Conserva Markdown/texto plano.
 */
public final class NewsContentSanitizer {

    private static final Pattern HTML_TAG = Pattern.compile("(?i)<[^>]*>");
    private static final Pattern CONTROL = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");

    private NewsContentSanitizer() {
    }

    public static String sanitize(String raw) {
        if (raw == null) {
            return "";
        }
        String withoutTags = HTML_TAG.matcher(raw).replaceAll("");
        return CONTROL.matcher(withoutTags).replaceAll("").trim();
    }
}
