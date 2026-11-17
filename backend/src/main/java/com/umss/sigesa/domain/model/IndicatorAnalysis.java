package com.umss.sigesa.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorAnalysis {

    private UUID id;
    private UUID indicatorId;
    private UUID processId;
    private FodaQuadrant fodaQuadrant;
    private ComplianceJudgment compliance;
    private String synthesis;
    private UUID authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isComplete() {
        return fodaQuadrant != null
                && compliance != null
                && synthesis != null
                && !synthesis.isBlank();
    }

    public void apply(FodaQuadrant quadrant, ComplianceJudgment judgment, String text, UUID author, LocalDateTime now) {
        this.fodaQuadrant = quadrant;
        this.compliance = judgment;
        this.synthesis = text;
        this.authorId = author;
        this.updatedAt = now;
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}
