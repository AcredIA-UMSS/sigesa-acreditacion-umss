package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "tb_program_phase_summary")
public class ProgramPhaseSummaryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private ProgramDashboardSummaryEntity programSummary;

    @Column(name = "phase_id", nullable = false)
    private int phaseId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "percentage", nullable = false)
    private double percentage;

    @Column(name = "status", nullable = false)
    private String status;

    public ProgramPhaseSummaryEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ProgramDashboardSummaryEntity getProgramSummary() { return programSummary; }
    public void setProgramSummary(ProgramDashboardSummaryEntity programSummary) { this.programSummary = programSummary; }
    public int getPhaseId() { return phaseId; }
    public void setPhaseId(int phaseId) { this.phaseId = phaseId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
