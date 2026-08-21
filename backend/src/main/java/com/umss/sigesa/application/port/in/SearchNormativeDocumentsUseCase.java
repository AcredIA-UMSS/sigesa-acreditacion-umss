package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.model.normative.NormativeDocumentHit;

import java.util.List;

public interface SearchNormativeDocumentsUseCase {

    List<NormativeDocumentHit> search(String query, String templateType, int limit);
}
