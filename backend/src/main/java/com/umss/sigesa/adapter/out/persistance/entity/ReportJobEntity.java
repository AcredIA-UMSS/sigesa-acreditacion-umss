package com.umss.sigesa.adapter.out.persistance.entity;

import com.umss.sigesa.domain.model.ReportJobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_job")
@Getter
@Setter
@NoArgsConstructor
public class ReportJobEntity {

    @Id
    private UUID id;

    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;

    @Column(name = "faculty_id")
    private UUID facultyId;

    @Column(name = "program_id")
    private UUID programId;

    @Column(name = "management_year", nullable = false)
    private int managementYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportJobStatus status;

    @Column(name = "artifact_key", length = 500)
    private String artifactKey;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
