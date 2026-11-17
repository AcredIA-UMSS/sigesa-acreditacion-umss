package com.umss.sigesa.domain.model.observation;

import com.umss.sigesa.domain.exception.InvalidObservationStateException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProcessObservation {

    public static final int MIN_DESCRIPTION = 10;

    private final UUID id;
    private final UUID processId;
    private UUID evidenceId;
    private UUID indicatorId;
    private String description;
    private ObservationKind kind;
    private ObservationPriority priority;
    private ComplianceLevel compliance;
    private String recommendation;
    private final UUID authorId;
    private ObservationStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<ObservationMessage> messages = new ArrayList<>();

    private ProcessObservation(
            UUID id,
            UUID processId,
            UUID evidenceId,
            UUID indicatorId,
            String description,
            ObservationKind kind,
            ObservationPriority priority,
            ComplianceLevel compliance,
            String recommendation,
            UUID authorId,
            ObservationStatus status,
            Instant createdAt,
            Instant updatedAt,
            List<ObservationMessage> messages) {
        this.id = id;
        this.processId = processId;
        this.evidenceId = evidenceId;
        this.indicatorId = indicatorId;
        this.description = description;
        this.kind = kind;
        this.priority = priority;
        this.compliance = compliance;
        this.recommendation = recommendation;
        this.authorId = authorId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        if (messages != null) {
            this.messages.addAll(messages);
        }
    }

    public static ProcessObservation draft(
            UUID id,
            UUID processId,
            UUID evidenceId,
            UUID indicatorId,
            String description,
            ObservationKind kind,
            ObservationPriority priority,
            ComplianceLevel compliance,
            String recommendation,
            UUID authorId,
            Instant now) {
        requireText(description, "La descripción es obligatoria");
        requirePresent(kind, "El tipo de observación es obligatorio");
        requirePresent(priority, "La prioridad es obligatoria");
        requirePresent(compliance, "El cumplimiento es obligatorio");
        return new ProcessObservation(
                id, processId, evidenceId, indicatorId, description.trim(), kind, priority, compliance,
                blankToNull(recommendation), authorId, ObservationStatus.BORRADOR, now, now, List.of());
    }

    public static ProcessObservation restore(
            UUID id,
            UUID processId,
            UUID evidenceId,
            UUID indicatorId,
            String description,
            ObservationKind kind,
            ObservationPriority priority,
            ComplianceLevel compliance,
            String recommendation,
            UUID authorId,
            ObservationStatus status,
            Instant createdAt,
            Instant updatedAt,
            List<ObservationMessage> messages) {
        return new ProcessObservation(
                id, processId, evidenceId, indicatorId, description, kind, priority, compliance,
                recommendation, authorId, status, createdAt, updatedAt, messages);
    }

    public void updateDraft(
            UUID evidenceId,
            UUID indicatorId,
            String description,
            ObservationKind kind,
            ObservationPriority priority,
            ComplianceLevel compliance,
            String recommendation,
            UUID actorId,
            Instant now) {
        assertStatus(ObservationStatus.BORRADOR, "Solo un borrador se puede editar");
        if (!authorId.equals(actorId)) {
            throw new InvalidObservationStateException("OBSERVATION_AUTHOR", "Solo el autor puede editar el borrador");
        }
        requireText(description, "La descripción es obligatoria");
        requirePresent(kind, "El tipo de observación es obligatorio");
        requirePresent(priority, "La prioridad es obligatoria");
        requirePresent(compliance, "El cumplimiento es obligatorio");
        this.evidenceId = evidenceId;
        this.indicatorId = indicatorId;
        this.description = description.trim();
        this.kind = kind;
        this.priority = priority;
        this.compliance = compliance;
        this.recommendation = blankToNull(recommendation);
        this.updatedAt = now;
    }

    public void submit(Instant now) {
        assertStatus(ObservationStatus.BORRADOR, "Solo un borrador se puede enviar");
        if (description == null || description.trim().length() < MIN_DESCRIPTION) {
            throw new InvalidObservationStateException(
                    "OBSERVATION_INCOMPLETE",
                    "La descripción debe tener al menos " + MIN_DESCRIPTION + " caracteres");
        }
        if (recommendation == null || recommendation.isBlank()) {
            throw new InvalidObservationStateException(
                    "OBSERVATION_INCOMPLETE", "La recomendación es obligatoria para enviar");
        }
        this.status = ObservationStatus.ENVIADA;
        this.updatedAt = now;
    }

    public void startReview(Instant now) {
        assertStatus(ObservationStatus.ENVIADA, "Solo una observación enviada pasa a revisión");
        this.status = ObservationStatus.EN_REVISION;
        this.updatedAt = now;
    }

    public void reply(UUID authorId, String authorRole, String body, Instant now) {
        assertStatus(ObservationStatus.EN_REVISION, "La respuesta se registra en revisión");
        addMessage(authorId, authorRole, body, now);
        this.status = ObservationStatus.RESPONDIDA;
        this.updatedAt = now;
    }

    public void attend(UUID authorId, String authorRole, String body, Instant now) {
        assertStatus(ObservationStatus.RESPONDIDA, "Solo una observación respondida se marca atendida");
        addMessage(authorId, authorRole, body, now);
        this.status = ObservationStatus.ATENDIDA;
        this.updatedAt = now;
    }

    public void close(UUID authorId, String authorRole, String body, Instant now) {
        assertStatus(ObservationStatus.ATENDIDA, "Solo una observación atendida se cierra");
        addMessage(authorId, authorRole, body, now);
        this.status = ObservationStatus.CERRADA;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getProcessId() { return processId; }
    public UUID getEvidenceId() { return evidenceId; }
    public UUID getIndicatorId() { return indicatorId; }
    public String getDescription() { return description; }
    public ObservationKind getKind() { return kind; }
    public ObservationPriority getPriority() { return priority; }
    public ComplianceLevel getCompliance() { return compliance; }
    public String getRecommendation() { return recommendation; }
    public UUID getAuthorId() { return authorId; }
    public ObservationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<ObservationMessage> getMessages() { return List.copyOf(messages); }

    private void addMessage(UUID messageAuthorId, String authorRole, String body, Instant now) {
        requireText(body, "El comentario es obligatorio");
        messages.add(new ObservationMessage(UUID.randomUUID(), messageAuthorId, authorRole, body.trim(), now));
    }

    private void assertStatus(ObservationStatus expected, String message) {
        if (status != expected) {
            throw new InvalidObservationStateException("INVALID_OBSERVATION_STATE", message);
        }
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidObservationStateException("OBSERVATION_INCOMPLETE", message);
        }
    }

    private static void requirePresent(Object value, String message) {
        if (value == null) {
            throw new InvalidObservationStateException("OBSERVATION_INCOMPLETE", message);
        }
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
