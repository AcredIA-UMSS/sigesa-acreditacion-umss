package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "document_assets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_document_assets_folio", columnNames = "folio_code"),
                @UniqueConstraint(name = "uk_document_assets_process_hash", columnNames = {"process_id", "content_hash"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class DocumentAssetEntity {

    @Id
    private UUID id;

    @Column(name = "process_id", nullable = false)
    private UUID processId;

    @Column(name = "folio_code", nullable = false, length = 64)
    private String folioCode;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "is_institutional", nullable = false)
    private boolean institutional;

    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
