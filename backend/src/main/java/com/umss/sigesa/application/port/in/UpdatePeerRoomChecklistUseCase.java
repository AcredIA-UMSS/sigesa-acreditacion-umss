package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.PeerRoomChecklistItem;

import java.util.List;
import java.util.UUID;

public interface UpdatePeerRoomChecklistUseCase {

    record ChecklistUpdateItem(String itemCode, boolean checked) {
    }

    List<PeerRoomChecklistItem> update(
            UUID processId,
            List<ChecklistUpdateItem> items,
            UUID actorId,
            List<String> roles,
            List<UUID> jwtProgramScope);
}
