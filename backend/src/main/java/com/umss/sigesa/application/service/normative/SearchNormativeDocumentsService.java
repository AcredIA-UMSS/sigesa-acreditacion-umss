package com.umss.sigesa.application.service.normative;

import com.umss.sigesa.application.model.normative.NormativeDocumentHit;
import com.umss.sigesa.application.port.in.SearchNormativeDocumentsUseCase;
import com.umss.sigesa.application.port.out.NormativeDocumentSearchPort;

import java.util.List;

public class SearchNormativeDocumentsService implements SearchNormativeDocumentsUseCase {

    private final NormativeDocumentSearchPort searchPort;

    public SearchNormativeDocumentsService(NormativeDocumentSearchPort searchPort) {
        this.searchPort = searchPort;
    }

    @Override
    public List<NormativeDocumentHit> search(String query, String templateType, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return searchPort.search(query.trim(), templateType, limit);
    }
}
