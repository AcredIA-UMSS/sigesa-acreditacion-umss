package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.AccreditationProcess;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessQueryPort {

    record ProcessListItem(
            UUID id,
            UUID careerId,
            UUID templateId,
            String status,
            java.time.LocalDateTime startDate
    ) {
    }

    record ProcessStatsRow(String status, String operationalMode) {
    }

    List<ProcessListItem> findAllSummaryItems();

    List<ProcessListItem> findSummaryItemsByCareerIds(List<UUID> careerIds);

    List<ProcessStatsRow> findAllStatsRows();

    List<ProcessStatsRow> findStatsRowsByCareerIds(List<UUID> careerIds);

    Optional<AccreditationProcess> findDetailById(UUID id);
}
