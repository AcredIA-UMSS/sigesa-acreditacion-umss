package com.umss.sigesa.adapter.in.web.dto;

import com.umss.sigesa.domain.model.PeerVisit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PeerVisitResponseDto(
        UUID id,
        UUID processId,
        LocalDate startsOn,
        LocalDate endsOn,
        String venue,
        String room,
        String status,
        String notes,
        List<PeerVisitItemResponseDto> items,
        LocalDateTime updatedAt
) {

    public static PeerVisitResponseDto from(PeerVisit visit) {
        List<PeerVisitItemResponseDto> items = visit.getItems() == null
                ? List.of()
                : visit.getItems().stream().map(PeerVisitItemResponseDto::from).toList();
        return new PeerVisitResponseDto(
                visit.getId(),
                visit.getProcessId(),
                visit.getStartsOn(),
                visit.getEndsOn(),
                visit.getVenue(),
                visit.getRoom(),
                visit.getStatus() != null ? visit.getStatus().name() : null,
                visit.getNotes(),
                items,
                visit.getUpdatedAt());
    }
}
