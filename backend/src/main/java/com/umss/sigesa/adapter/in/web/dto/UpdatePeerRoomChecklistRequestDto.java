package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Actualización de checklist Sala de Pares")
public class UpdatePeerRoomChecklistRequestDto {

    @Getter
    @Setter
    public static class Item {

        private String itemCode;
        private boolean checked;
    }

    private List<Item> items;
}
