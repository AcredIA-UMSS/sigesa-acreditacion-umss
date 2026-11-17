package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.PeerRoomChecklistItemEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataPeerRoomChecklistItemRepository;
import com.umss.sigesa.application.port.out.PeerRoomChecklistPort;
import com.umss.sigesa.domain.model.PeerRoomChecklistItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PeerRoomChecklistItemJpaAdapter implements PeerRoomChecklistPort {

    private final SpringDataPeerRoomChecklistItemRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<PeerRoomChecklistItem> findByProcessId(UUID processId) {
        return repository.findByProcessIdOrderByItemCodeAsc(processId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PeerRoomChecklistItem> findByProcessIdAndItemCode(UUID processId, String itemCode) {
        return repository.findByProcessIdAndItemCode(processId, itemCode).map(this::toDomain);
    }

    @Override
    @Transactional
    public PeerRoomChecklistItem save(PeerRoomChecklistItem item) {
        PeerRoomChecklistItemEntity entity = repository.findById(item.getId())
                .orElseGet(PeerRoomChecklistItemEntity::new);
        entity.setId(item.getId());
        entity.setProcessId(item.getProcessId());
        entity.setItemCode(item.getItemCode());
        entity.setLabel(item.getLabel());
        entity.setChecked(item.isChecked());
        entity.setCheckedBy(item.getCheckedBy());
        entity.setCheckedAt(item.getCheckedAt());
        return toDomain(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean allChecked(UUID processId) {
        long total = repository.countByProcessId(processId);
        if (total == 0) {
            return false;
        }
        return repository.countByProcessIdAndCheckedTrue(processId) == total;
    }

    private PeerRoomChecklistItem toDomain(PeerRoomChecklistItemEntity entity) {
        return PeerRoomChecklistItem.builder()
                .id(entity.getId())
                .processId(entity.getProcessId())
                .itemCode(entity.getItemCode())
                .label(entity.getLabel())
                .checked(entity.isChecked())
                .checkedBy(entity.getCheckedBy())
                .checkedAt(entity.getCheckedAt())
                .build();
    }
}
