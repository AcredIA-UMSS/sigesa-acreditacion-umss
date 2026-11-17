package com.umss.sigesa.domain.model;

import com.umss.sigesa.domain.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImprovementPlanItem {

    private UUID id;
    private UUID processId;
    private UUID sourceIndicatorId;
    private String objective;
    private String activity;
    private UUID responsibleId;
    private LocalDate deadline;
    private int progress;
    private String justification;
    private UUID progressEvidenceAssetId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ImprovementPlanItem createManual(
            UUID processId,
            String objective,
            String activity,
            UUID responsibleId,
            LocalDate deadline,
            Integer progress,
            LocalDateTime now) {
        int normalizedProgress = progress == null ? 0 : progress;
        assertValid(objective, normalizedProgress);
        return ImprovementPlanItem.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .objective(objective.trim())
                .activity(blankToNull(activity))
                .responsibleId(responsibleId)
                .deadline(deadline)
                .progress(normalizedProgress)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static ImprovementPlanItem createFromWeakness(
            UUID processId,
            UUID sourceIndicatorId,
            String synthesis,
            LocalDateTime now) {
        String objective = (synthesis != null && !synthesis.isBlank())
                ? synthesis.trim()
                : "Atender debilidad FODA del indicador";
        assertValid(objective, 0);
        return ImprovementPlanItem.builder()
                .id(UUID.randomUUID())
                .processId(processId)
                .sourceIndicatorId(sourceIndicatorId)
                .objective(objective)
                .progress(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void applyUpdate(
            String newObjective,
            String newActivity,
            UUID newResponsibleId,
            LocalDate newDeadline,
            Integer newProgress,
            String newJustification,
            UUID newProgressEvidenceAssetId,
            LocalDateTime now) {
        int normalizedProgress = newProgress == null ? this.progress : newProgress;
        assertValid(newObjective, normalizedProgress);
        this.objective = newObjective.trim();
        this.activity = blankToNull(newActivity);
        this.responsibleId = newResponsibleId;
        this.deadline = newDeadline;
        this.progress = normalizedProgress;
        this.justification = blankToNull(newJustification);
        this.progressEvidenceAssetId = newProgressEvidenceAssetId;
        this.updatedAt = now;
    }

    public static void assertValid(String objective, int progress) {
        if (objective == null || objective.isBlank()) {
            throw new ValidationException("El objetivo no puede estar vacío.");
        }
        if (progress < 0 || progress > 100) {
            throw new ValidationException("El avance debe estar entre 0 y 100.");
        }
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
