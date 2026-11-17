package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.AppUserJpaRepository;
import com.umss.sigesa.adapter.out.persistance.InAppNotificationJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AppUserEntity;
import com.umss.sigesa.adapter.out.persistance.NotificationOutboxJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.InAppNotificationJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.NotificationOutboxJpaEntity;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Demo dual-channel notification fixtures for local/E2E testing (PR-IMPL-053).
 * Idempotent by stable IDs. Profile {@code !prod}.
 */
@Component
@Profile("!prod")
@Order(101)
public class NotificationSeedDataLoader implements ApplicationRunner {

    private static final UUID NOTIF_JD_PROCESS_CLOSED = UUID.fromString("b0000000-0000-4000-8000-000000000001");
    private static final UUID NOTIF_JD_WELCOME = UUID.fromString("b0000000-0000-4000-8000-000000000002");
    private static final UUID NOTIF_JD_DEADLINE = UUID.fromString("b0000000-0000-4000-8000-000000000003");
    private static final UUID NOTIF_CC_REJECTED = UUID.fromString("b0000000-0000-4000-8000-000000000011");
    private static final UUID NOTIF_CC_APPROVED = UUID.fromString("b0000000-0000-4000-8000-000000000012");
    private static final UUID NOTIF_CC_DEADLINE = UUID.fromString("b0000000-0000-4000-8000-000000000013");
    private static final UUID NOTIF_CC_REJECTED_READ = UUID.fromString("b0000000-0000-4000-8000-000000000014");
    private static final UUID NOTIF_CC_ASSIGNED = UUID.fromString("b0000000-0000-4000-8000-000000000015");
    private static final UUID NOTIF_CC_UNASSIGNED = UUID.fromString("b0000000-0000-4000-8000-000000000016");
    private static final UUID NOTIF_TD_UPLOADED = UUID.fromString("b0000000-0000-4000-8000-000000000021");
    private static final UUID NOTIF_TD_SUBSANATED = UUID.fromString("b0000000-0000-4000-8000-000000000022");
    private static final UUID NOTIF_TD_UPLOADED_2 = UUID.fromString("b0000000-0000-4000-8000-000000000023");

    private static final UUID OUTBOX_JD_PROCESS_CLOSED = UUID.fromString("b1000000-0000-4000-8000-000000000001");
    private static final UUID OUTBOX_CC_REJECTED = UUID.fromString("b1000000-0000-4000-8000-000000000011");
    private static final UUID OUTBOX_TD_UPLOADED = UUID.fromString("b1000000-0000-4000-8000-000000000021");

    private static final String INDICATOR_1 = EvidenceDataLoader.SEED_INDICATOR_ID.toString();
    private static final String INDICATOR_2 = EvidenceDataLoader.SEED_INDICATOR_2_ID.toString();
    private static final String INDICATOR_3 = EvidenceDataLoader.SEED_INDICATOR_3_ID.toString();
    private static final String PROCESS_ACTIVE = DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE.toString();
    private static final String PROCESS_CLOSED = DevSeedData.PROCESS_CEUB_CLOSED.toString();
    private static final String PROGRAM = DevSeedData.PROGRAM_INF_SIS.toString();

    private final InAppNotificationJpaRepository notificationRepository;
    private final NotificationOutboxJpaRepository outboxRepository;
    private final AppUserJpaRepository userRepository;

    public NotificationSeedDataLoader(
            InAppNotificationJpaRepository notificationRepository,
            NotificationOutboxJpaRepository outboxRepository,
            AppUserJpaRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.outboxRepository = outboxRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        LocalDateTime now = LocalDateTime.now();

        // Resolve actual demo user IDs by email (legacy DBs may not match AuthDataLoader stable UUIDs).
        UUID jdUserId = resolveUserId(AuthDataLoader.SEED_JD_EMAIL, AuthDataLoader.SEED_JD_USER_ID);
        UUID ccUserId = resolveUserId(AuthDataLoader.SEED_CC_EMAIL, AuthDataLoader.SEED_CC_USER_ID);
        UUID tdUserId = resolveUserId(AuthDataLoader.SEED_TD_EMAIL, AuthDataLoader.SEED_TD_USER_ID);

        // --- [JD] ---
        seedInAppIfAbsent(
                NOTIF_JD_PROCESS_CLOSED,
                jdUserId,
                "ProcessClosed",
                "Proceso cerrado",
                "Ingeniería de Sistemas cerró el proceso de acreditación.",
                "/executive/semaphore?processId=" + PROCESS_CLOSED,
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PROCESS_CEUB_CLOSED,
                null,
                now.minusHours(2));
        seedInAppIfAbsent(
                NOTIF_JD_WELCOME,
                jdUserId,
                "SystemNotice",
                "Bienvenido a SIGESA",
                "Revise el semáforo institucional para el seguimiento ejecutivo.",
                "/executive/semaphore",
                null,
                null,
                null,
                now.minusDays(1));
        seedInAppIfAbsent(
                NOTIF_JD_DEADLINE,
                jdUserId,
                "DeadlineApproaching",
                "Plazo próximo",
                "Hay un plazo próximo en Ingeniería de Sistemas (seguimiento ejecutivo).",
                "/executive/semaphore?processId=" + PROCESS_ACTIVE,
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE,
                null,
                now.minusMinutes(45));

        // --- [CC] ---
        seedInAppIfAbsent(
                NOTIF_CC_REJECTED,
                ccUserId,
                "IndicatorRejected",
                "Atención: Indicador observado",
                "El indicador " + shortId(INDICATOR_1) + " fue observado y requiere subsanación.",
                "/procesos/" + PROCESS_ACTIVE + "/indicadores/" + INDICATOR_1,
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE,
                null,
                now.minusHours(1));
        seedInAppIfAbsent(
                NOTIF_CC_APPROVED,
                ccUserId,
                "IndicatorApproved",
                "Indicador aprobado",
                "El indicador " + shortId(INDICATOR_2) + " fue aprobado.",
                "/procesos/" + PROCESS_ACTIVE + "/indicadores/" + INDICATOR_2,
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE,
                null,
                now.minusHours(3));
        seedInAppIfAbsent(
                NOTIF_CC_DEADLINE,
                ccUserId,
                "DeadlineApproaching",
                "Plazo próximo",
                "Hay un plazo próximo en el programa " + shortId(PROGRAM) + ".",
                "/procesos/" + PROCESS_ACTIVE + "/indicadores/" + INDICATOR_1,
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE,
                null,
                now.minusMinutes(20));
        seedInAppIfAbsent(
                NOTIF_CC_REJECTED_READ,
                ccUserId,
                "IndicatorRejected",
                "Atención: Indicador observado",
                "Observación histórica ya leída (seed para contraste UI).",
                "/procesos/" + PROCESS_ACTIVE + "/indicadores/" + INDICATOR_3,
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE,
                now.minusDays(2),
                now.minusDays(3));
        seedInAppIfAbsent(
                NOTIF_CC_ASSIGNED,
                ccUserId,
                "ProcessAssigned",
                "Proceso asignado",
                "Usted fue asignado como responsable del proceso " + shortId(PROCESS_ACTIVE) + ".",
                "/procesos/" + PROCESS_ACTIVE,
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE,
                null,
                now.minusMinutes(5));
        seedInAppIfAbsent(
                NOTIF_CC_UNASSIGNED,
                ccUserId,
                "ProcessUnassigned",
                "Responsabilidad retirada",
                "Ya no es responsable del proceso " + shortId(PROCESS_CLOSED) + ".",
                "",
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PROCESS_CEUB_CLOSED,
                null,
                now.minusMinutes(3));

        // --- [TD] ---
        seedInAppIfAbsent(
                NOTIF_TD_UPLOADED,
                tdUserId,
                "EvidenceUploaded",
                "Nueva evidencia",
                "Se cargó evidencia para el indicador " + shortId(INDICATOR_1) + ".",
                "/dashboard/technician/inbox?programId=" + PROGRAM + "&status=SUBIDO",
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE,
                null,
                now.minusMinutes(30));
        seedInAppIfAbsent(
                NOTIF_TD_SUBSANATED,
                tdUserId,
                "EvidenceSubsanated",
                "Evidencia subsanada",
                "Se subsanó el indicador " + shortId(INDICATOR_1) + ".",
                "/dashboard/technician/inbox?programId=" + PROGRAM + "&status=SUBSANADO",
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE,
                null,
                now.minusMinutes(10));
        seedInAppIfAbsent(
                NOTIF_TD_UPLOADED_2,
                tdUserId,
                "EvidenceUploaded",
                "Nueva evidencia",
                "Se cargó evidencia para el indicador " + shortId(INDICATOR_2) + ".",
                "/dashboard/technician/inbox?programId=" + PROGRAM + "&status=SUBIDO",
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE,
                null,
                now.minusHours(5));

        // --- EMAIL outbox samples (Mailpit/sigesa-smtp) ---
        seedOutboxIfAbsent(
                OUTBOX_JD_PROCESS_CLOSED,
                "ProcessClosed",
                jdUserId,
                AuthDataLoader.SEED_JD_EMAIL,
                "JD",
                "Proceso de acreditación cerrado",
                "El proceso " + PROCESS_CLOSED + " quedó en estado CLOSED. Consulte el semáforo ejecutivo.",
                "/executive/semaphore?processId=" + PROCESS_CLOSED,
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PROCESS_CEUB_CLOSED,
                "seed:ProcessClosed:" + PROCESS_CLOSED + ":JD:" + jdUserId,
                now.minusHours(2));
        seedOutboxIfAbsent(
                OUTBOX_CC_REJECTED,
                "IndicatorRejected",
                ccUserId,
                AuthDataLoader.SEED_CC_EMAIL,
                "CC",
                "Indicador observado (" + shortId(INDICATOR_1) + ")",
                "Se registró una observación sobre el indicador " + INDICATOR_1
                        + ". Revise la justificación y cargue la evidencia de subsanación.",
                "/procesos/" + PROCESS_ACTIVE + "/indicadores/" + INDICATOR_1,
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE,
                "seed:IndicatorRejected:" + INDICATOR_1 + ":CC:" + ccUserId,
                now.minusHours(1));
        seedOutboxIfAbsent(
                OUTBOX_TD_UPLOADED,
                "EvidenceUploaded",
                tdUserId,
                AuthDataLoader.SEED_TD_EMAIL,
                "TD",
                "Nueva evidencia registrada - Indicador " + shortId(INDICATOR_1),
                "Se ha registrado una nueva evidencia para el indicador " + INDICATOR_1
                        + " en el programa " + PROGRAM + ".",
                "/dashboard/technician/inbox?programId=" + PROGRAM + "&status=SUBIDO",
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE,
                "seed:EvidenceUploaded:" + INDICATOR_1 + ":TD:" + tdUserId,
                now.minusMinutes(30));
    }


    private UUID resolveUserId(String email, UUID fallback) {
        return userRepository.findByEmail(email)
                .map(AppUserEntity::getId)
                .orElse(fallback);
    }

    private void seedInAppIfAbsent(
            UUID id,
            UUID userId,
            String eventType,
            String title,
            String body,
            String deepLink,
            UUID programId,
            UUID processId,
            LocalDateTime readAt,
            LocalDateTime createdAt) {
        if (notificationRepository.existsById(id)) {
            // Repair ownership when demo users pre-existed with non-stable UUIDs.
            notificationRepository.findById(id).ifPresent(existing -> {
                if (userId != null && !userId.equals(existing.getUserId())) {
                    existing.setUserId(userId);
                    notificationRepository.save(existing);
                }
            });
            return;
        }
        notificationRepository.save(InAppNotificationJpaEntity.builder()
                .id(id)
                .userId(userId)
                .eventType(eventType)
                .title(title)
                .body(body)
                .deepLink(deepLink)
                .programId(programId)
                .processId(processId)
                .readAt(readAt)
                .createdAt(createdAt)
                .build());
    }

    private void seedOutboxIfAbsent(
            UUID id,
            String eventType,
            UUID recipientId,
            String recipientEmail,
            String recipientRole,
            String subject,
            String body,
            String deepLink,
            UUID programId,
            UUID processId,
            String idempotencyKey,
            LocalDateTime createdAt) {
        if (outboxRepository.existsById(id) || outboxRepository.existsByIdempotencyKey(idempotencyKey)) {
            return;
        }
        outboxRepository.save(NotificationOutboxJpaEntity.builder()
                .id(id)
                .eventType(eventType)
                .recipientId(recipientId)
                .recipientEmail(recipientEmail)
                .recipientRole(recipientRole)
                .subject(subject)
                .body(body)
                .deepLink(deepLink)
                .programId(programId)
                .processId(processId)
                .channel("EMAIL")
                .idempotencyKey(idempotencyKey)
                .status("PENDING")
                .retryCount(0)
                .maxRetries(5)
                .createdAt(createdAt)
                .build());
    }

    private static String shortId(String id) {
        if (id == null || id.isBlank()) {
            return "—";
        }
        return id.length() <= 8 ? id : id.substring(0, 8);
    }
}
