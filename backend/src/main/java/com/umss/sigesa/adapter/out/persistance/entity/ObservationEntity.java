package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "observation")
public class ObservationEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "evidence_version_id", nullable = false)
    private UUID evidenceVersionId;

    @Column(name = "observer_id", nullable = false)
    private UUID observerId;

    @Column(name = "role_code", nullable = false, length = 10)
    private String roleCode;

    @Column(name = "observations", nullable = false, length = 2000)
    private String observations;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "supersedes_id")
    private UUID supersedesId;

    @Column(name = "program_id")
    private UUID programId;

    @Column(name = "indicator_id")
    private String indicatorId;

    @Column(name = "indicator_code")
    private String indicatorCode;

    @Column(name = "indicator_title")
    private String indicatorTitle;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "phase_id")
    private Integer phaseId;

    @Column(name = "status")
    private String status;

    @Column(name = "remediation_url", length = 512)
    private String remediationUrl;

    public ObservationEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getEvidenceVersionId() { return evidenceVersionId; }
    public void setEvidenceVersionId(UUID evidenceVersionId) { this.evidenceVersionId = evidenceVersionId; }

    public UUID getObserverId() { return observerId; }
    public void setObserverId(UUID observerId) { this.observerId = observerId; }

    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UUID getSupersedesId() { return supersedesId; }
    public void setSupersedesId(UUID supersedesId) { this.supersedesId = supersedesId; }

    public UUID getProgramId() { return programId; }
    public void setProgramId(UUID programId) { this.programId = programId; }

    public String getIndicatorId() { return indicatorId; }
    public void setIndicatorId(String indicatorId) { this.indicatorId = indicatorId; }

    public String getIndicatorCode() { return indicatorCode; }
    public void setIndicatorCode(String indicatorCode) { this.indicatorCode = indicatorCode; }

    public String getIndicatorTitle() { return indicatorTitle; }
    public void setIndicatorTitle(String indicatorTitle) { this.indicatorTitle = indicatorTitle; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Integer getPhaseId() { return phaseId; }
    public void setPhaseId(Integer phaseId) { this.phaseId = phaseId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemediationUrl() { return remediationUrl; }
    public void setRemediationUrl(String remediationUrl) { this.remediationUrl = remediationUrl; }
}
