package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_program_dashboard_summary")
public class ProgramDashboardSummaryEntity {

    @Id
    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(name = "program_name", nullable = false)
    private String programName;

    @Column(name = "total_indicators", nullable = false)
    private int totalIndicators;

    @Column(name = "overall_progress_percentage", nullable = false)
    private double overallProgressPercentage;

    @Column(name = "approved_evidences", nullable = false)
    private int approvedEvidences;

    @Column(name = "rejected_evidences", nullable = false)
    private int rejectedEvidences;

    @Column(name = "pending_observations", nullable = false)
    private int pendingObservations;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "programSummary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProgramPhaseSummaryEntity> phases = new ArrayList<>();

    public ProgramDashboardSummaryEntity() {}

    public UUID getProgramId() { return programId; }
    public void setProgramId(UUID programId) { this.programId = programId; }
    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }
    public int getTotalIndicators() { return totalIndicators; }
    public void setTotalIndicators(int totalIndicators) { this.totalIndicators = totalIndicators; }
    public double getOverallProgressPercentage() { return overallProgressPercentage; }
    public void setOverallProgressPercentage(double overallProgressPercentage) { this.overallProgressPercentage = overallProgressPercentage; }
    public int getApprovedEvidences() { return approvedEvidences; }
    public void setApprovedEvidences(int approvedEvidences) { this.approvedEvidences = approvedEvidences; }
    public int getRejectedEvidences() { return rejectedEvidences; }
    public void setRejectedEvidences(int rejectedEvidences) { this.rejectedEvidences = rejectedEvidences; }
    public int getPendingObservations() { return pendingObservations; }
    public void setPendingObservations(int pendingObservations) { this.pendingObservations = pendingObservations; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public List<ProgramPhaseSummaryEntity> getPhases() { return phases; }
    public void setPhases(List<ProgramPhaseSummaryEntity> phases) { this.phases = phases; }
}
