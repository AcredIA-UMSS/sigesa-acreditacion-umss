package com.umss.sigesa.application.port.out;

import com.umss.sigesa.application.model.normative.NormativeDocumentHit;

import java.util.List;

public interface NormativeDocumentSearchPort {

    List<NormativeDocumentHit> search(String query, String templateType, int limit);
}
