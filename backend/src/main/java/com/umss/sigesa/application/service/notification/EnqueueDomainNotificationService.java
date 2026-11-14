package com.umss.sigesa.application.service.notification;

import com.umss.sigesa.application.port.in.EnqueueDomainNotificationUseCase;
import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.application.port.out.InAppNotificationPort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NotificationOutboxWritePort;
import com.umss.sigesa.application.port.out.NotificationRecipientResolverPort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.notification.DomainNotificationCommand;
import com.umss.sigesa.domain.model.notification.InAppNotification;
import com.umss.sigesa.domain.model.notification.NotificationChannel;
import com.umss.sigesa.domain.model.notification.NotificationEventType;
import com.umss.sigesa.domain.model.notification.NotificationRecipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class EnqueueDomainNotificationService implements EnqueueDomainNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(EnqueueDomainNotificationService.class);

    private final NotificationRecipientResolverPort recipientResolver;
    private final NotificationRecipientResolverService recipientResolverService;
    private final NotificationOutboxWritePort outboxWritePort;
    private final InAppNotificationPort inAppNotificationPort;
    private final AccreditationProcessPort processPort;
    private final ProgramCatalogPort programCatalogPort;
    private final TemplatePort templatePort;
    private final NormativeHierarchyQueryPort normativeHierarchyQueryPort;

    public EnqueueDomainNotificationService(
            NotificationRecipientResolverPort recipientResolver,
            NotificationOutboxWritePort outboxWritePort,
            InAppNotificationPort inAppNotificationPort,
            AccreditationProcessPort processPort,
            ProgramCatalogPort programCatalogPort,
            TemplatePort templatePort,
            NormativeHierarchyQueryPort normativeHierarchyQueryPort) {
        this.recipientResolver = recipientResolver;
        this.recipientResolverService = recipientResolver instanceof NotificationRecipientResolverService service
                ? service
                : null;
        this.outboxWritePort = outboxWritePort;
        this.inAppNotificationPort = inAppNotificationPort;
        this.processPort = processPort;
        this.programCatalogPort = programCatalogPort;
        this.templatePort = templatePort;
        this.normativeHierarchyQueryPort = normativeHierarchyQueryPort;
    }

    /**
     * Backward-compatible constructor for unit tests that do not exercise label enrichment.
     */
    public EnqueueDomainNotificationService(
            NotificationRecipientResolverPort recipientResolver,
            NotificationOutboxWritePort outboxWritePort,
            InAppNotificationPort inAppNotificationPort,
            AccreditationProcessPort processPort) {
        this(recipientResolver, outboxWritePort, inAppNotificationPort, processPort, null, null, null);
    }

    @Override
    public void enqueue(DomainNotificationCommand command) {
        if (command.eventType() == NotificationEventType.ProcessClosed
                && !isProcessAlreadyClosed(command.processId())) {
            log.info("NOTIFICATION_SKIP_PROCESS_CLOSED processId={} reason=status_not_CLOSED",
                    command.processId());
            return;
        }

        List<NotificationRecipient> recipients = resolveRecipients(command);
        if (recipients.isEmpty()) {
            log.warn("NOTIFICATION_NO_RECIPIENTS eventType={} aggregateId={} programId={} processId={}",
                    command.eventType(), command.aggregateId(), command.programId(), command.processId());
            return;
        }

        DomainNotificationCommand enriched = enrichWithBusinessLabels(command);
        NotificationMessageFactory.MessageBundle messages = NotificationMessageFactory.build(enriched);
        String aggregateKey = NotificationMessageFactory.aggregateKey(enriched);
        String payloadJson = toPayloadJson(enriched);
        LocalDateTime now = LocalDateTime.now();

        for (NotificationRecipient recipient : recipients) {
            try {
                persistDualChannel(enriched, recipient, aggregateKey, messages, payloadJson, now);
            } catch (RuntimeException ex) {
                log.warn("NOTIFICATION_ENQUEUE_SKIPPED eventType={} userId={} reason={}",
                        command.eventType(), recipient.userId(), ex.getMessage());
            }
        }
    }

    private DomainNotificationCommand enrichWithBusinessLabels(DomainNotificationCommand command) {
        Map<String, String> payload = new LinkedHashMap<>(command.payload());

        UUID programId = command.programId();
        UUID processId = command.processId();
        Optional<AccreditationProcess> processOpt = processId != null
                ? processPort.findById(processId)
                : Optional.empty();

        if (programId == null) {
            programId = processOpt.map(AccreditationProcess::getCareerId).orElse(null);
        }

        putProgramLabels(payload, programId);
        putProcessLabels(payload, processOpt.orElse(null));
        putIndicatorLabels(payload, command);
        putLevel1Labels(payload, command);

        return new DomainNotificationCommand(
                command.eventType(),
                command.aggregateId(),
                command.programId() != null ? command.programId() : programId,
                command.processId(),
                payload);
    }

    private void putProgramLabels(Map<String, String> payload, UUID programId) {
        if (programId == null || programCatalogPort == null) {
            return;
        }
        if (hasText(payload.get("programName")) || hasText(payload.get("careerName"))) {
            return;
        }
        programCatalogPort.findById(programId).ifPresent(program -> {
            if (hasText(program.name())) {
                payload.put("programName", program.name());
                payload.put("careerName", program.name());
            }
            if (hasText(program.code())) {
                payload.put("programCode", program.code());
            }
        });
    }

    private void putProcessLabels(Map<String, String> payload, AccreditationProcess process) {
        if (process == null || templatePort == null) {
            return;
        }
        if (hasText(payload.get("processLabel")) || hasText(payload.get("templateName"))) {
            return;
        }
        UUID templateId = process.getTemplateId();
        if (templateId == null) {
            return;
        }
        Optional<Template> template = templatePort.findMetadataById(templateId);
        if (template.isEmpty()) {
            template = templatePort.findById(templateId);
        }
        template.ifPresent(t -> {
            String templateName = t.getName();
            String templateType = t.getType();
            String programName = firstNonBlank(payload.get("programName"), payload.get("careerName"));
            String processLabel;
            if (hasText(templateName) && hasText(programName)) {
                processLabel = templateName + " — " + programName;
            } else if (hasText(templateName)) {
                processLabel = templateName;
            } else if (hasText(templateType) && hasText(programName)) {
                processLabel = templateType + " — " + programName;
            } else {
                processLabel = firstNonBlank(templateType, programName);
            }
            if (hasText(templateName)) {
                payload.put("templateName", templateName);
            }
            if (hasText(templateType)) {
                payload.put("templateType", templateType);
            }
            if (hasText(processLabel)) {
                payload.put("processLabel", processLabel);
            }
        });
    }

    private void putIndicatorLabels(Map<String, String> payload, DomainNotificationCommand command) {
        if (hasText(payload.get("indicatorLabel")) || hasText(payload.get("indicatorCode"))) {
            return;
        }
        if (normativeHierarchyQueryPort == null) {
            return;
        }
        UUID indicatorId = resolveIndicatorId(command);
        if (indicatorId == null) {
            return;
        }

        Optional<NormativeHierarchyQueryPort.NormativeIndicatorContext> context =
                normativeHierarchyQueryPort.findIndicatorContext(indicatorId);
        if (context.isPresent()) {
            NormativeHierarchyQueryPort.NormativeIndicatorContext ctx = context.get();
            String code = ctx.code();
            String description = ctx.description();
            if (hasText(code)) {
                payload.put("indicatorCode", code);
            }
            if (hasText(ctx.level1Name())) {
                payload.putIfAbsent("level1Name", ctx.level1Name());
            }
            payload.put("indicatorLabel", formatIndicatorLabel(code, description));
            return;
        }

        normativeHierarchyQueryPort.findIndicatorById(indicatorId).ifPresent(indicator -> {
            String code = indicator.getCode();
            String description = indicator.getDescription();
            if (hasText(code)) {
                payload.put("indicatorCode", code);
            }
            payload.put("indicatorLabel", formatIndicatorLabel(code, description));
        });
    }

    private void putLevel1Labels(Map<String, String> payload, DomainNotificationCommand command) {
        if (hasText(payload.get("level1Name")) || normativeHierarchyQueryPort == null) {
            return;
        }
        if (command.eventType() != NotificationEventType.Level1Completed) {
            return;
        }
        UUID level1Id = null;
        String raw = command.payloadValue("level1Id");
        if (hasText(raw)) {
            try {
                level1Id = UUID.fromString(raw.trim());
            } catch (IllegalArgumentException ignored) {
                // fall through to aggregateId
            }
        }
        if (level1Id == null) {
            level1Id = command.aggregateId();
        }
        normativeHierarchyQueryPort.findLevel1Context(level1Id).ifPresent(ctx -> {
            if (hasText(ctx.level1Name())) {
                payload.put("level1Name", ctx.level1Name());
            }
        });
    }

    private static UUID resolveIndicatorId(DomainNotificationCommand command) {
        return switch (command.eventType()) {
            case IndicatorRejected, IndicatorApproved, EvidenceUploaded, EvidenceSubsanated, DeadlineApproaching -> {
                String raw = command.payloadValue("indicatorId");
                if (hasText(raw)) {
                    try {
                        yield UUID.fromString(raw.trim());
                    } catch (IllegalArgumentException ex) {
                        yield command.aggregateId();
                    }
                }
                yield command.aggregateId();
            }
            default -> null;
        };
    }

    private static String formatIndicatorLabel(String code, String description) {
        if (hasText(code) && hasText(description)) {
            String trimmed = description.trim();
            if (trimmed.length() > 120) {
                trimmed = trimmed.substring(0, 117) + "...";
            }
            return code + " — " + trimmed;
        }
        if (hasText(code)) {
            return code;
        }
        if (hasText(description)) {
            String trimmed = description.trim();
            return trimmed.length() > 120 ? trimmed.substring(0, 117) + "..." : trimmed;
        }
        return "";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (hasText(primary)) {
            return primary.trim();
        }
        if (hasText(fallback)) {
            return fallback.trim();
        }
        return "";
    }

    private void persistDualChannel(
            DomainNotificationCommand command,
            NotificationRecipient recipient,
            String aggregateKey,
            NotificationMessageFactory.MessageBundle messages,
            String payloadJson,
            LocalDateTime now) {
        String emailKey = NotificationOutboxWritePort.idempotencyKey(
                command.eventType(), aggregateKey, recipient.userId(), NotificationChannel.EMAIL);
        String inAppKey = NotificationOutboxWritePort.idempotencyKey(
                command.eventType(), aggregateKey, recipient.userId(), NotificationChannel.IN_APP);

        Optional<UUID> emailOutboxId = outboxWritePort.saveIfAbsent(
                emailKey,
                command.eventType(),
                NotificationChannel.EMAIL,
                recipient,
                command.programId(),
                command.processId(),
                messages.emailSubject(),
                messages.emailBody(),
                messages.deepLink(),
                payloadJson,
                "PENDING");

        Optional<UUID> inAppMarkerId = outboxWritePort.saveIfAbsent(
                inAppKey,
                command.eventType(),
                NotificationChannel.IN_APP,
                recipient,
                command.programId(),
                command.processId(),
                messages.inAppTitle(),
                messages.inAppBody(),
                messages.deepLink(),
                payloadJson,
                "SENT");

        if (inAppMarkerId.isEmpty()) {
            return;
        }

        UUID correlatedOutboxId = emailOutboxId.orElse(inAppMarkerId.get());
        UUID inAppId = UUID.randomUUID();
        inAppNotificationPort.save(new InAppNotification(
                inAppId,
                correlatedOutboxId,
                recipient.userId(),
                command.eventType().name(),
                messages.inAppTitle(),
                truncate(messages.inAppBody(), 500),
                messages.deepLink(),
                command.programId(),
                command.processId(),
                null,
                now));

        log.info("NOTIFICATION_DUAL_ENQUEUED eventType={} userId={} emailOutboxId={} inAppId={}",
                command.eventType(), recipient.userId(), emailOutboxId.orElse(null), inAppId);
    }

    private List<NotificationRecipient> resolveRecipients(DomainNotificationCommand command) {
        return switch (command.eventType()) {
            case IndicatorRejected, IndicatorApproved, DeadlineApproaching, Level1Completed ->
                    resolveCcUnion(command.programId(), command.processId());
            case EvidenceUploaded, EvidenceSubsanated ->
                    recipientResolver.resolveForProgram(command.programId(), Role.TD);
            case ProcessClosed ->
                    recipientResolver.resolveActiveByRole(Role.JD);
            case ProcessAssigned, ProcessUnassigned ->
                    resolveSpecificUser(command);
        };
    }

    private List<NotificationRecipient> resolveSpecificUser(DomainNotificationCommand command) {
        String raw = command.payloadValue("assigneeUserId");
        if (raw == null || raw.isBlank()) {
            raw = command.payloadValue("removedUserId");
        }
        if (raw == null || raw.isBlank()) {
            log.warn("NOTIFICATION_MISSING_USER eventType={} processId={}",
                    command.eventType(), command.processId());
            return List.of();
        }
        try {
            UUID userId = UUID.fromString(raw.trim());
            return recipientResolver.resolveUser(userId)
                    .map(List::of)
                    .orElseGet(List::of);
        } catch (IllegalArgumentException ex) {
            log.warn("NOTIFICATION_INVALID_USER_ID eventType={} raw={}", command.eventType(), raw);
            return List.of();
        }
    }

    private List<NotificationRecipient> resolveCcUnion(UUID programId, UUID processId) {
        if (recipientResolverService != null) {
            return recipientResolverService.resolveCcForProgram(programId, processId);
        }
        Map<UUID, NotificationRecipient> byUser = new LinkedHashMap<>();
        if (processId != null) {
            recipientResolver.resolveProcessResponsible(processId)
                    .ifPresent(r -> byUser.put(r.userId(), r));
        }
        for (NotificationRecipient recipient : recipientResolver.resolveForProgram(programId, Role.CC)) {
            byUser.putIfAbsent(recipient.userId(), recipient);
        }
        return List.copyOf(byUser.values());
    }

    private boolean isProcessAlreadyClosed(UUID processId) {
        if (processId == null) {
            return false;
        }
        return processPort.findById(processId)
                .map(AccreditationProcess::getStatus)
                .map(status -> ProcessStatus.CLOSED.name().equals(status))
                .orElse(false);
    }

    private static String toPayloadJson(DomainNotificationCommand command) {
        Map<String, String> map = new LinkedHashMap<>(command.payload());
        if (command.programId() != null) {
            map.putIfAbsent("programId", command.programId().toString());
        }
        if (command.processId() != null) {
            map.putIfAbsent("processId", command.processId().toString());
        }
        map.putIfAbsent("aggregateId", command.aggregateId().toString());
        return map.entrySet().stream()
                .map(e -> "\"" + escape(e.getKey()) + "\":\"" + escape(e.getValue()) + "\"")
                .collect(Collectors.joining(",", "{", "}"));
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
