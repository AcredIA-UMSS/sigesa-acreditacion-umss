package com.umss.sigesa.application.service.normative;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Tesauro de dominio CEUB / ARCU-SUR para expansión de consultas sobre chunks ya indexados.
 */
public final class NormativeDomainThesaurus {

    private static final Map<String, List<String>> CANONICAL_TO_SYNONYMS = new LinkedHashMap<>();
    private static final Map<String, String> ALIAS_TO_CANONICAL = new LinkedHashMap<>();
    private static final Set<String> GENERIC_TERMS = Set.of(
            "normativa", "procedimiento", "procedimientos", "sistema", "informacion", "información",
            "documentacion", "documentación", "manual", "proceso", "procesos", "requisito",
            "requisitos", "indicacion", "indicación");

    static {
        add("pares", List.of(
                "par", "pares", "comité de pares", "comite de pares", "evaluadores",
                "evaluador", "evaluadores externos", "evaluador externo", "equipo evaluador",
                "bipe", "banco internacional de pares"));
        add("visita", List.of(
                "visita", "visita in situ", "visita del comité", "visita del comite",
                "visita de pares"));
        add("cronograma", List.of("cronograma", "calendario", "plazos", "fechas"));
        add("convocatoria", List.of("convocatoria", "llamado", "apertura de convocatoria"));
        add("autoevaluacion", List.of(
                "autoevaluación", "autoevaluacion", "informe de autoevaluación",
                "informe de autoevaluacion"));
        add("comite", List.of("comité", "comite", "comisión", "comision"));
        add("resolucion", List.of("resolución", "resolucion", "dictamen formal"));
        add("candidatura", List.of("candidatura", "postulación", "postulacion"));
        add("capacitacion", List.of("capacitación", "capacitacion", "formación de pares", "formacion de pares"));
        add("diagnostico", List.of(
                "diagnóstico", "diagnostico", "diagnóstico institucional", "diagnostico institucional"));
        add("matriz", List.of("matriz", "matriz de evidencias", "matriz evidencias"));
        add("ceub", List.of("ceub", "duea"));
        add("arcu", List.of("arcu", "arcu-sur", "arcusur"));
    }

    private NormativeDomainThesaurus() {
    }

    public static String canonicalOf(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return ALIAS_TO_CANONICAL.get(fold(token));
    }

    public static List<String> synonymsOf(String canonical) {
        if (canonical == null) {
            return List.of();
        }
        return CANONICAL_TO_SYNONYMS.getOrDefault(fold(canonical), List.of());
    }

    public static boolean isGeneric(String token) {
        return token != null && GENERIC_TERMS.contains(fold(token));
    }

    public static boolean isDomainTerm(String token) {
        return canonicalOf(token) != null;
    }

    static String fold(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ü", "u")
                .replace("ñ", "n");
    }

    private static void add(String canonical, List<String> aliases) {
        String key = fold(canonical);
        CANONICAL_TO_SYNONYMS.put(key, List.copyOf(aliases));
        for (String alias : aliases) {
            ALIAS_TO_CANONICAL.putIfAbsent(fold(alias), key);
            for (String part : alias.split("\\s+")) {
                if (part.length() >= 4) {
                    ALIAS_TO_CANONICAL.putIfAbsent(fold(part), key);
                }
            }
        }
        ALIAS_TO_CANONICAL.putIfAbsent(key, key);
    }

    static Set<String> expandLexemes(String canonical) {
        LinkedHashSet<String> expanded = new LinkedHashSet<>();
        expanded.add(canonical);
        for (String synonym : synonymsOf(canonical)) {
            for (String part : synonym.split("\\s+")) {
                if (part.length() >= 3) {
                    expanded.add(part);
                }
            }
        }
        return expanded;
    }
}
