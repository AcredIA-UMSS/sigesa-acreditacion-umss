package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level1NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level2NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level3NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.NormativeIndicatorJpaEntity;
import com.umss.sigesa.domain.model.IndicatorState;
import jakarta.persistence.EntityManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Indicadores listos para dictamen en {@code /technician/inbox}.
 * El proceso queda fuera de {@code ACTIVE} para no reemplazar el semáforo de demostración.
 */
@Component
@Profile("!prod")
@Order(120)
public class TechnicianInboxDataLoader implements ApplicationRunner {

    private static final UUID ACTOR = UUID.fromString("00000000-0000-4000-8000-000000000021");

    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    public TechnicianInboxDataLoader(EntityManager entityManager,
                                     PlatformTransactionManager transactionManager) {
        this.entityManager = entityManager;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            transactionTemplate.executeWithoutResult(status -> seed());
        } catch (Exception ex) {
            System.err.println("Technician inbox seeding skipped: " + ex.getMessage());
        }
    }

    private void seed() {
        Long alreadySeeded = entityManager.createQuery(
                        "SELECT COUNT(i) FROM NormativeIndicatorJpaEntity i WHERE i.code = 'IND-M-01'",
                        Long.class)
                .getSingleResult();
        if (alreadySeeded != null && alreadySeeded > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        seedCareer(
                DevSeedData.PROGRAM_ING_MECANICA,
                "Formación académica",
                now,
                new InboxIndicator("IND-M-01", "Plan de estudios vigente", IndicatorState.SUBIDO, "plan_estudios_mecanica.pdf", 1),
                new InboxIndicator("IND-M-02", "Inventario de laboratorios", IndicatorState.SUBIDO, "inventario_laboratorios.pdf", 2),
                new InboxIndicator("IND-M-03", "Sílabos de asignaturas troncales", IndicatorState.SUBSANADO, "silabos_mecanica_v2.pdf", 3),
                new InboxIndicator("IND-M-04", "Convenios de práctica industrial", IndicatorState.OBSERVADO, "convenio_practica.pdf", 4),
                new InboxIndicator("IND-M-05", "Reglamento de evaluación", IndicatorState.APROBADO, "reglamento_evaluacion.pdf", 5));
        seedCareer(
                DevSeedData.PROGRAM_CONTADURIA,
                "Gestión académica",
                now,
                new InboxIndicator("IND-C-01", "Malla curricular auditada", IndicatorState.SUBIDO, "malla_contaduria.pdf", 1),
                new InboxIndicator("IND-C-02", "Informe de prácticas profesionales", IndicatorState.SUBSANADO, "practicas_contaduria.pdf", 2),
                new InboxIndicator("IND-C-03", "Planta docente a tiempo completo", IndicatorState.SUBIDO, "planta_docente.pdf", 3),
                new InboxIndicator("IND-C-04", "Estados financieros auditados", IndicatorState.APROBADO, "estados_financieros.pdf", 4));
    }

    private void seedCareer(UUID careerId, String dimension, LocalDateTime now, InboxIndicator... indicators) {
        AccreditationProcessJpaEntity process = AccreditationProcessJpaEntity.builder()
                .careerId(careerId)
                .templateId(DevSeedData.TEMPLATE_CEUB_2026)
                .status("COLLECTION")
                .startDate(now)
                .operationalMode("ACTIVE")
                .build();
        entityManager.persist(process);

        Level1NodeJpaEntity level1 = Level1NodeJpaEntity.builder()
                .name(dimension)
                .order(1)
                .description("Dimensión de demostración para la bandeja de dictamen")
                .createdAt(now)
                .updatedAt(now)
                .process(process)
                .build();
        entityManager.persist(level1);

        Level2NodeJpaEntity level2 = Level2NodeJpaEntity.builder()
                .name("Criterio de calidad")
                .order(1)
                .description("Criterio de demostración")
                .createdAt(now)
                .updatedAt(now)
                .level1Node(level1)
                .build();
        entityManager.persist(level2);

        Level3NodeJpaEntity level3 = Level3NodeJpaEntity.builder()
                .name("Medios de verificación")
                .order(1)
                .description("Subcriterio de demostración")
                .createdAt(now)
                .updatedAt(now)
                .level2Node(level2)
                .build();
        entityManager.persist(level3);

        for (InboxIndicator item : indicators) {
            persistIndicator(level3, item, now);
        }
    }

    private void persistIndicator(Level3NodeJpaEntity level3, InboxIndicator item, LocalDateTime now) {
        NormativeIndicatorJpaEntity indicator = NormativeIndicatorJpaEntity.builder()
                .code(item.code())
                .description(item.description())
                .weight(BigDecimal.ONE)
                .order(item.order())
                .referenceUrl("https://duea.umss.edu.bo/demo/" + item.code().toLowerCase())
                .status(item.status().name())
                .createdAt(now.minusDays(item.order()))
                .updatedAt(now.minusHours(item.order()))
                .level3Node(level3)
                .build();
        entityManager.persist(indicator);
        entityManager.flush();

        UUID evidenceId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        EvidenceEntity evidence = new EvidenceEntity();
        evidence.setId(evidenceId);
        evidence.setNormativeIndicatorId(indicator.getId());
        evidence.setCreatedAt(now.minusDays(1));
        entityManager.persist(evidence);

        EvidenceVersionEntity version = new EvidenceVersionEntity();
        version.setId(versionId);
        version.setEvidenceId(evidenceId);
        version.setVersionNumber(item.status() == IndicatorState.SUBSANADO ? 2 : 1);
        version.setContentHash("d".repeat(64));
        version.setDescription(item.description());
        version.setStorageKey(item.filename());
        version.setOriginalFilename(item.filename());
        version.setCreatedBy(ACTOR);
        version.setCreatedAt(now.minusHours(item.order()));
        version.setBlobPurged(false);
        entityManager.persist(version);

        evidence.setLatestVersionId(versionId);
    }

    private record InboxIndicator(String code, String description, IndicatorState status, String filename, int order) {
    }
}
