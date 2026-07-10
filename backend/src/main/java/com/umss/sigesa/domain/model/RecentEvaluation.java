package com.umss.sigesa.domain.model;

public record RecentEvaluation(
        String evidenceId,
        String program,
        String revisionDate,
        String result
) {}
