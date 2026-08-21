package com.umss.sigesa.application.model.normative;

public record NormativeDocumentHit(
        String title,
        String templateType,
        String phaseName,
        String subphaseName,
        String sourceUrl,
        String snippet,
        double score
) {
}
