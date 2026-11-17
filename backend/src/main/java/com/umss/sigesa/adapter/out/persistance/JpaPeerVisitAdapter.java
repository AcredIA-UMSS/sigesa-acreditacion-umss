package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.PeerVisitItemJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.PeerVisitJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataPeerVisitRepository;
import com.umss.sigesa.application.port.out.PeerVisitPort;
import com.umss.sigesa.domain.model.PeerVisit;
import com.umss.sigesa.domain.model.PeerVisitItem;
import com.umss.sigesa.domain.model.PeerVisitItemType;
import com.umss.sigesa.domain.model.PeerVisitStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaPeerVisitAdapter implements PeerVisitPort {

    private final SpringDataPeerVisitRepository repository;

    public JpaPeerVisitAdapter(SpringDataPeerVisitRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PeerVisit> findByProcessId(UUID processId) {
        return repository.findByProcessIdWithItems(processId).map(this::toDomain);
    }

    @Override
    @Transactional
    public PeerVisit save(PeerVisit visit) {
        PeerVisitJpaEntity entity = repository.findByProcessIdWithItems(visit.getProcessId())
                .orElseGet(PeerVisitJpaEntity::new);
        entity.setId(visit.getId());
        entity.setProcessId(visit.getProcessId());
        entity.setStartsOn(visit.getStartsOn());
        entity.setEndsOn(visit.getEndsOn());
        entity.setVenue(visit.getVenue());
        entity.setRoom(visit.getRoom());
        entity.setStatus(visit.getStatus() != null ? visit.getStatus().name() : PeerVisitStatus.SCHEDULED.name());
        entity.setNotes(visit.getNotes());
        entity.setCreatedBy(visit.getCreatedBy());
        entity.setCreatedAt(visit.getCreatedAt());
        entity.setUpdatedAt(visit.getUpdatedAt());
        if (entity.getItems() == null) {
            entity.setItems(new ArrayList<>());
        }
        entity.getItems().clear();
        if (visit.getItems() != null) {
            for (PeerVisitItem item : visit.getItems()) {
                PeerVisitItemJpaEntity itemEntity = PeerVisitItemJpaEntity.builder()
                        .id(item.getId() != null ? item.getId() : UUID.randomUUID())
                        .visit(entity)
                        .type(item.getType() != null ? item.getType().name() : PeerVisitItemType.DOCUMENT.name())
                        .title(item.getTitle())
                        .scheduledAt(item.getScheduledAt())
                        .location(item.getLocation())
                        .notes(item.getNotes())
                        .done(item.isDone())
                        .order(item.getOrder())
                        .build();
                entity.getItems().add(itemEntity);
            }
        }
        return toDomain(repository.save(entity));
    }

    private PeerVisit toDomain(PeerVisitJpaEntity entity) {
        List<PeerVisitItem> items = new ArrayList<>();
        if (entity.getItems() != null) {
            entity.getItems().forEach(item -> items.add(PeerVisitItem.builder()
                    .id(item.getId())
                    .type(PeerVisitItemType.valueOf(item.getType()))
                    .title(item.getTitle())
                    .scheduledAt(item.getScheduledAt())
                    .location(item.getLocation())
                    .notes(item.getNotes())
                    .done(item.isDone())
                    .order(item.getOrder())
                    .build()));
        }
        return PeerVisit.builder()
                .id(entity.getId())
                .processId(entity.getProcessId())
                .startsOn(entity.getStartsOn())
                .endsOn(entity.getEndsOn())
                .venue(entity.getVenue())
                .room(entity.getRoom())
                .status(PeerVisitStatus.valueOf(entity.getStatus()))
                .notes(entity.getNotes())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .items(items)
                .build();
    }
}
