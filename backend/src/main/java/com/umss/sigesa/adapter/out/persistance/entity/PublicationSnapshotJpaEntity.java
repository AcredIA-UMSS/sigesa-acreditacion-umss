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

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "publication_snapshot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicationSnapshotJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String slug;

    @Column(name = "program_name", nullable = false)
    private String programName;

    @Column(name = "accreditation_status", nullable = false, length = 32)
    private String accreditationStatus;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @Column(nullable = false)
    @Builder.Default
    private boolean published = false;

    @Column(name = "certificate_published", nullable = false)
    @Builder.Default
    private boolean certificatePublished = false;

    @Column(length = 255)
    private String agency;

    @Column(name = "resolution_number", length = 64)
    private String resolutionNumber;
}
