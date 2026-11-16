package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "peer_visit_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeerVisitItemJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private PeerVisitJpaEntity visit;

    @Column(name = "item_type", nullable = false, length = 24)
    private String type;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(length = 255)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private boolean done;

    @Column(name = "item_order", nullable = false)
    private int order;
}
