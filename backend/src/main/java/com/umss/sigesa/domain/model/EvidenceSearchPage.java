package com.umss.sigesa.domain.model;

import java.util.List;

public record EvidenceSearchPage(
        List<EvidenceSearchHit> items,
        long total,
        int page,
        int size
) {
}
