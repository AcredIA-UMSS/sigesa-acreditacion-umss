package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.PeerRoomChecklistItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPeerRoomChecklistItemRepository
        extends JpaRepository<PeerRoomChecklistItemEntity, UUID> {

    List<PeerRoomChecklistItemEntity> findByProcessIdOrderByItemCodeAsc(UUID processId);

    Optional<PeerRoomChecklistItemEntity> findByProcessIdAndItemCode(UUID processId, String itemCode);

    long countByProcessId(UUID processId);

    long countByProcessIdAndCheckedTrue(UUID processId);
}
