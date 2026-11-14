package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.PeerRoomChecklistItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PeerRoomChecklistPort {

    List<PeerRoomChecklistItem> findByProcessId(UUID processId);

    Optional<PeerRoomChecklistItem> findByProcessIdAndItemCode(UUID processId, String itemCode);

    PeerRoomChecklistItem save(PeerRoomChecklistItem item);

    boolean allChecked(UUID processId);
}
