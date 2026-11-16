package com.umss.sigesa.adapter.in.web.dto;

import com.umss.sigesa.domain.model.PeerVisitItem;

import java.time.LocalDateTime;
import java.util.UUID;

public record PeerVisitItemResponseDto(
        UUID id,
        String type,
        String title,
        LocalDateTime scheduledAt,
        String location,
        String notes,
        boolean done,
        int order
) {

    public static PeerVisitItemResponseDto from(PeerVisitItem item) {
        return new PeerVisitItemResponseDto(
                item.getId(),
                item.getType() != null ? item.getType().name() : null,
                item.getTitle(),
                item.getScheduledAt(),
                item.getLocation(),
                item.getNotes(),
                item.isDone(),
                item.getOrder());
    }
}
