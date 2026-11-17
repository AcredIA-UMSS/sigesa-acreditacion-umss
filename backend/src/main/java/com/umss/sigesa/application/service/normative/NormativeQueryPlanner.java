package com.umss.sigesa.application.service.normative;

import com.umss.sigesa.application.model.normative.NormativeSearchPlan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Elige las 2–3 claves de mayor peso, expande sinónimos y arma consultas AND/OR para FTS.
 */
public final class NormativeQueryPlanner {

    private static final int MAX_SELECTED = 3;
    private static final Set<String> ARCU_HINTS = Set.of(
            "arcu", "arcu-sur", "arcusur", "pares", "visita", "convocatoria", "cronograma", "bipe");
    private static final Set<String> CEUB_HINTS = Set.of("ceub", "duea", "matriz", "diagnostico");

    private NormativeQueryPlanner() {
    }

    public static NormativeSearchPlan plan(String rawQuery, String requestedTemplateType) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return NormativeSearchPlan.empty("");
        }
        List<String> tokens = NormativeSearchQueryNormalizer.significantTerms(rawQuery);
        if (tokens.isEmpty()) {
            return NormativeSearchPlan.empty(rawQuery);
        }

        List<WeightedTerm> scored = new ArrayList<>();
        List<String> discarded = new ArrayList<>();
        LinkedHashSet<String> seenCanonical = new LinkedHashSet<>();
        for (String token : tokens) {
            int weight = weightOf(token);
            String canonical = NormativeDomainThesaurus.canonicalOf(token);
            String key = canonical != null ? canonical : token.toLowerCase(Locale.ROOT);
            if (weight <= 0) {
                discarded.add(token);
                continue;
            }
            if (!seenCanonical.add(key)) {
                continue;
            }
            scored.add(new WeightedTerm(key, weight));
        }
        scored.sort(Comparator.comparingInt(WeightedTerm::weight).reversed());

        List<String> selected = scored.stream()
                .limit(MAX_SELECTED)
                .map(WeightedTerm::term)
                .toList();
        if (selected.isEmpty()) {
            selected = tokens.stream().limit(MAX_SELECTED).toList();
        }

        LinkedHashSet<String> expanded = new LinkedHashSet<>(selected);
        for (String term : selected) {
            expanded.addAll(NormativeDomainThesaurus.expandLexemes(term));
        }

        String andQuery = String.join(" ", selected);
        String orQuery = String.join(" ", expanded);
        String hint = firstNonBlank(requestedTemplateType, inferTemplateType(tokens, selected));
        return new NormativeSearchPlan(
                List.copyOf(selected),
                List.copyOf(expanded),
                List.copyOf(discarded),
                andQuery,
                orQuery,
                hint,
                "WEIGHTED_SYNONYM");
    }

    static int weightOf(String token) {
        if (token == null || token.isBlank()) {
            return 0;
        }
        if (NormativeDomainThesaurus.isGeneric(token)) {
            return -2;
        }
        int weight = 1;
        if (NormativeDomainThesaurus.isDomainTerm(token)) {
            weight += 3;
        }
        String folded = NormativeDomainThesaurus.fold(token);
        if (ARCU_HINTS.contains(folded) || CEUB_HINTS.contains(folded) || folded.contains("arcu")) {
            weight += 2;
        }
        if (token.length() >= 6) {
            weight += 1;
        }
        return weight;
    }

    private static String inferTemplateType(List<String> tokens, List<String> selected) {
        List<String> pool = new ArrayList<>(tokens);
        pool.addAll(selected);
        boolean arcu = false;
        boolean ceub = false;
        for (String token : pool) {
            String folded = NormativeDomainThesaurus.fold(token);
            if (ARCU_HINTS.contains(folded) || folded.contains("arcu")) {
                arcu = true;
            }
            if (CEUB_HINTS.contains(folded)) {
                ceub = true;
            }
        }
        if (arcu && !ceub) {
            return "ARCU-SUR";
        }
        if (ceub && !arcu) {
            return "CEUB";
        }
        return null;
    }

    private static String firstNonBlank(String requested, String inferred) {
        if (requested != null && !requested.isBlank()) {
            return requested.trim();
        }
        return inferred;
    }

    private record WeightedTerm(String term, int weight) {
    }
}
