package com.umss.sigesa.application.port.out;

import com.umss.sigesa.adapter.in.web.dto.EvidenceSearchDetailDto;
import java.util.List;
import java.util.UUID;

public interface SearchEvidenceQueryPort {
    List<EvidenceSearchDetailDto> executeSearch(String termino, String dimension, List<UUID> programScope);
    String getLastExecutedSql();
}
