package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(description = "Ítem de checklist Sala de Pares")
public class PeerRoomChecklistItemDto {

    private UUID id;
    private String itemCode;
    private String label;
    private boolean checked;
    private UUID checkedBy;
    private LocalDateTime checkedAt;
}
