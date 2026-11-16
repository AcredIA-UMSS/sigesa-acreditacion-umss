package com.umss.sigesa.application.model.normative;

import java.util.List;

/**
 * Plan de recuperación sobre chunks indexados: términos elegidos, expansión y estrategia FTS.
 */
public record NormativeSearchPlan(
        List<String> selectedTerms,
        List<String> expandedTerms,
        List<String> discardedTerms,
        String andQuery,
        String orQuery,
        String templateTypeHint,
        String strategy
) {
    public static NormativeSearchPlan empty(String rawQuery) {
        String fallback = rawQuery == null ? "" : rawQuery.trim();
        return new NormativeSearchPlan(
                List.of(),
                List.of(),
                List.of(),
                fallback,
                fallback,
                null,
                "FALLBACK_RAW");
    }

    public boolean hasSelectedTerms() {
        return selectedTerms != null && !selectedTerms.isEmpty();
    }
}
