package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.PeerRoomChecklistItem;

import java.util.List;
import java.util.UUID;

public interface GetPeerRoomChecklistUseCase {

    List<PeerRoomChecklistItem> get(UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope);
}
