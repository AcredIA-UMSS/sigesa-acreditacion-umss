package com.umss.sigesa.domain.model;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class NewsSlug {

    private static final Pattern NON_LATIN = Pattern.compile("[^a-z0-9]+");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

    private NewsSlug() {
    }

    public static String fromTitle(String title) {
        if (title == null || title.isBlank()) {
            return "noticia";
        }
        String normalized = Normalizer.normalize(title.trim(), Normalizer.Form.NFD);
        String ascii = DIACRITICS.matcher(normalized).replaceAll("");
        String kebab = NON_LATIN.matcher(ascii.toLowerCase(Locale.ROOT)).replaceAll("-");
        kebab = kebab.replaceAll("^-+", "").replaceAll("-+$", "");
        return kebab.isBlank() ? "noticia" : kebab;
    }
}
