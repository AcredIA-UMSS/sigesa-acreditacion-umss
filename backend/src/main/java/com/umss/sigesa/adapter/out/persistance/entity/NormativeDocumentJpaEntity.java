package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "normative_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormativeDocumentJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 512)
    private String title;

    @Column(name = "template_type", nullable = false, length = 32)
    private String templateType;

    @Column(name = "phase_name", length = 256)
    private String phaseName;

    @Column(name = "subphase_name", length = 256)
    private String subphaseName;

    @Column(name = "source_url", length = 2048)
    private String sourceUrl;

    @Column(name = "body_text", nullable = false, columnDefinition = "TEXT")
    private String bodyText;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
