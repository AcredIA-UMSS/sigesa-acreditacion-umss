package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tb_observation")
public class ObservationEntity {

    @Id
    @Column(name = "observation_id", nullable = false)
    private String observationId;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(name = "indicator_id", nullable = false)
    private String indicatorId;

    @Column(name = "indicator_code", nullable = false)
    private String indicatorCode;

    @Column(name = "indicator_title", nullable = false)
    private String indicatorTitle;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "phase_id")
    private Integer phaseId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "remediation_url", length = 512)
    private String remediationUrl;

    public ObservationEntity() {}

    public String getObservationId() { return observationId; }
    public void setObservationId(String observationId) { this.observationId = observationId; }
    public UUID getProgramId() { return programId; }
    public void setProgramId(UUID programId) { this.programId = programId; }
    public String getIndicatorId() { return indicatorId; }
    public void setIndicatorId(String indicatorId) { this.indicatorId = indicatorId; }
    public String getIndicatorCode() { return indicatorCode; }
    public void setIndicatorCode(String indicatorCode) { this.indicatorCode = indicatorCode; }
    public String getIndicatorTitle() { return indicatorTitle; }
    public void setIndicatorTitle(String indicatorTitle) { this.indicatorTitle = indicatorTitle; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public Integer getPhaseId() { return phaseId; }
    public void setPhaseId(Integer phaseId) { this.phaseId = phaseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemediationUrl() { return remediationUrl; }
    public void setRemediationUrl(String remediationUrl) { this.remediationUrl = remediationUrl; }
}
