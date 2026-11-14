package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "peer_room_checklist_items")
@Getter
@Setter
@NoArgsConstructor
public class PeerRoomChecklistItemEntity {

    @Id
    private UUID id;

    @Column(name = "process_id", nullable = false)
    private UUID processId;

    @Column(name = "item_code", nullable = false, length = 64)
    private String itemCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String label;

    @Column(nullable = false)
    private boolean checked;

    @Column(name = "checked_by")
    private UUID checkedBy;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;
}
