package com.umss.sigesa.domain.model;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum MethodologicalStageCode {
    PREPARATORY(1, "Preparatoria y administrativa"),
    COLLECTION(2, "Recolección de información"),
    SYSTEMATIZATION(3, "Sistematización y análisis"),
    DRAFTING(4, "Elaboración documentos clave"),
    PRESENTATION(5, "Presentación y validación"),
    EXTERNAL_PREP(6, "Preparación evaluación externa"),
    POST_ACCREDITATION(7, "Seguimiento post-acreditación");

    private final int order;
    private final String displayName;

    MethodologicalStageCode(int order, String displayName) {
        this.order = order;
        this.displayName = displayName;
    }

    public int order() {
        return order;
    }

    public String displayName() {
        return displayName;
    }

    public static List<MethodologicalStageCode> canonicalOrder() {
        return Arrays.stream(values())
                .sorted(Comparator.comparingInt(MethodologicalStageCode::order))
                .toList();
    }
}
