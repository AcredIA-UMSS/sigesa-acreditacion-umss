package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "template_phases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplatePhaseJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "phase_order", nullable = false)
    private Integer order;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private TemplateJpaEntity template;

    @OneToMany(mappedBy = "templatePhase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TemplateSubphaseJpaEntity> subphases = new ArrayList<>();
}
