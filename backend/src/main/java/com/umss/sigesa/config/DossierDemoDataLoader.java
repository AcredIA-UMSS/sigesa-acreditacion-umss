package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.EvidenceJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.DocumentAssetEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEvidenceLinkEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level1NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level2NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level3NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.NormativeIndicatorJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataDocumentAssetRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataIndicatorEvidenceLinkRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataLevel1NodeRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataLevel2NodeRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataLevel3NodeRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataNormativeIndicatorRepository;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.PhaseState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Seeder para la demostración de la exportación del Dossier Digital (FEAT-11 / Issue #73).
 * Puebla la jerarquía normativa completa (Dimensiones, Componentes, Criterios e Indicadores)
 * y genera DocumentAssets con evidencias físicas reales almacenadas en blobStorage
 * para el proceso activo de Ingeniería de Sistemas (PROCESS_INF_SIS_CEUB_ACTIVE).
 */
@Component
@Profile("!prod & !test")
@Order(110)
public class DossierDemoDataLoader implements ApplicationRunner {

    private static final Object LOCK = new Object();
    private static final Logger log = LoggerFactory.getLogger(DossierDemoDataLoader.class);

    private final SpringDataAccreditationProcessRepository processRepository;
    private final SpringDataLevel1NodeRepository level1Repository;
    private final SpringDataLevel2NodeRepository level2Repository;
    private final SpringDataLevel3NodeRepository level3Repository;
    private final SpringDataNormativeIndicatorRepository indicatorRepository;
    private final SpringDataDocumentAssetRepository assetRepository;
    private final SpringDataIndicatorEvidenceLinkRepository linkRepository;
    private final EvidenceJpaRepository evidenceRepository;
    private final EvidenceBlobStoragePort blobStorage;
    private final TransactionTemplate transactionTemplate;

    public DossierDemoDataLoader(
            SpringDataAccreditationProcessRepository processRepository,
            SpringDataLevel1NodeRepository level1Repository,
            SpringDataLevel2NodeRepository level2Repository,
            SpringDataLevel3NodeRepository level3Repository,
            SpringDataNormativeIndicatorRepository indicatorRepository,
            SpringDataDocumentAssetRepository assetRepository,
            SpringDataIndicatorEvidenceLinkRepository linkRepository,
            EvidenceJpaRepository evidenceRepository,
            EvidenceBlobStoragePort blobStorage,
            PlatformTransactionManager transactionManager) {
        this.processRepository = processRepository;
        this.level1Repository = level1Repository;
        this.level2Repository = level2Repository;
        this.level3Repository = level3Repository;
        this.indicatorRepository = indicatorRepository;
        this.assetRepository = assetRepository;
        this.linkRepository = linkRepository;
        this.evidenceRepository = evidenceRepository;
        this.blobStorage = blobStorage;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        synchronized (LOCK) {
            try {
                transactionTemplate.execute(status -> {
                    seedDossierShowcaseData();
                    return null;
                });
            } catch (Exception ex) {
                log.warn("Dossier showcase seeding omitido o completado por otra transacción: {}", ex.getMessage());
            }
        }
    }

    private void seedDossierShowcaseData() {
        UUID processId = DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE;

        if (level1Repository.existsByProcessId(processId)) {
            log.info("Dossier showcase data ya existe para el proceso {}", processId);
            return;
        }

        AccreditationProcessJpaEntity process = processRepository.findById(processId).orElseGet(() -> {
            AccreditationProcessJpaEntity newProcess = AccreditationProcessJpaEntity.builder()
                    .id(processId)
                    .careerId(DevSeedData.PROGRAM_INF_SIS)
                    .templateId(DevSeedData.TEMPLATE_CEUB_2026)
                    .status("ACTIVE")
                    .operationalMode("ACTIVE")
                    .startDate(LocalDateTime.now().minusMonths(3))
                    .build();
            return processRepository.save(newProcess);
        });

        LocalDateTime now = LocalDateTime.now();
        UUID coordinatorId = AuthDataLoader.SEED_CC_USER_ID;

        // -------------------------------------------------------------
        // DIMENSIÓN 1: Normas Generales y Definición Institucional
        // -------------------------------------------------------------
        Level1NodeJpaEntity dim1 = Level1NodeJpaEntity.builder()
                .id(UUID.randomUUID())
                .process(process)
                .name("Area 1: Normas Generales y Definicion Institucional")
                .order(1)
                .description("Marco normativo, estructura institucional y gobierno universitario de la carrera.")
                .status(PhaseState.ABIERTA.name())
                .createdAt(now)
                .updatedAt(now)
                .level2Nodes(new ArrayList<>())
                .build();
        level1Repository.save(dim1);

        Level2NodeJpaEntity comp1_1 = Level2NodeJpaEntity.builder()
                .id(UUID.randomUUID())
                .level1Node(dim1)
                .name("Componente 1.1: Marco Normativo y Gobierno")
                .order(1)
                .description("Resoluciones y estatutos constitutivos del programa académico.")
                .createdAt(now)
                .updatedAt(now)
                .level3Nodes(new ArrayList<>())
                .build();
        level2Repository.save(comp1_1);

        Level3NodeJpaEntity crit1_1_1 = Level3NodeJpaEntity.builder()
                .id(UUID.randomUUID())
                .level2Node(comp1_1)
                .name("Criterio 1.1.1: Estatuto Organico y Reglamentos")
                .order(1)
                .description("Existencia y aplicación de normativa orgánica formal.")
                .createdAt(now)
                .updatedAt(now)
                .indicators(new ArrayList<>())
                .build();
        level3Repository.save(crit1_1_1);

        NormativeIndicatorJpaEntity ind1_1_1 = createIndicator(
                crit1_1_1,
                "IND-1.1.1",
                "Estatuto Organico de la Universidad y Reglamentos de Carrera",
                1,
                IndicatorState.APROBADO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind1_1_1.getId(),
                coordinatorId,
                "Estatuto_Organico_UMSS_2025.pdf",
                "SIGESA-20260315-e1a0b3c4",
                "Estatuto Orgánico de la Universidad Mayor de San Simón actualizado al 2025",
                now
        );

        NormativeIndicatorJpaEntity ind1_1_2 = createIndicator(
                crit1_1_1,
                "IND-1.1.2",
                "Plan Estrategico de Desarrollo Institucional 2022-2026",
                2,
                IndicatorState.SUBIDO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind1_1_2.getId(),
                coordinatorId,
                "Plan_Estrategico_Facultad_2022_2026.pdf",
                "SIGESA-20260318-f2b1c4d5",
                "Plan Estratégico Quinquenal de la Facultad de Ciencias y Tecnología",
                now
        );

        // -------------------------------------------------------------
        // DIMENSIÓN 2: Proyecto Curricular y Plan de Estudios
        // -------------------------------------------------------------
        Level1NodeJpaEntity dim2 = Level1NodeJpaEntity.builder()
                .id(UUID.randomUUID())
                .process(process)
                .name("Area 2: Proyecto Curricular y Plan de Estudios")
                .order(2)
                .description("Diseño y actualización permanente del currículo y asignaturas de Ingeniería de Sistemas.")
                .status(PhaseState.ABIERTA.name())
                .createdAt(now)
                .updatedAt(now)
                .level2Nodes(new ArrayList<>())
                .build();
        level1Repository.save(dim2);

        Level2NodeJpaEntity comp2_1 = Level2NodeJpaEntity.builder()
                .id(UUID.randomUUID())
                .level1Node(dim2)
                .name("Componente 2.1: Diseno Curricular y Perfil Profesional")
                .order(1)
                .description("Malla curricular y congruencia con el mercado profesional de software.")
                .createdAt(now)
                .updatedAt(now)
                .level3Nodes(new ArrayList<>())
                .build();
        level2Repository.save(comp2_1);

        Level3NodeJpaEntity crit2_1_1 = Level3NodeJpaEntity.builder()
                .id(UUID.randomUUID())
                .level2Node(comp2_1)
                .name("Criterio 2.1.1: Malla Curricular y Planes Globales")
                .order(1)
                .description("Estructura de materias troncales, electivas y pre-requisitos.")
                .createdAt(now)
                .updatedAt(now)
                .indicators(new ArrayList<>())
                .build();
        level3Repository.save(crit2_1_1);

        NormativeIndicatorJpaEntity ind2_1_1 = createIndicator(
                crit2_1_1,
                "IND-2.1.1",
                "Plan de Estudios y Malla Curricular Vigente de Ingenieria de Sistemas",
                1,
                IndicatorState.APROBADO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind2_1_1.getId(),
                coordinatorId,
                "Malla_Curricular_Ing_Sistemas_2024.pdf",
                "SIGESA-20260402-a3c2d5e6",
                "Malla Curricular oficial aprobada por Resolución HCU 84/2024",
                now
        );

        NormativeIndicatorJpaEntity ind2_1_2 = createIndicator(
                crit2_1_1,
                "IND-2.1.2",
                "Programas Analiticos y Planes Globales de Ingenieria de Software",
                2,
                IndicatorState.SUBIDO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind2_1_2.getId(),
                coordinatorId,
                "Planes_Globales_Ingenieria_Software.pdf",
                "SIGESA-20260410-b4d3e6f7",
                "Planes Globales de las asignaturas de Taller de Ingeniería de Software I y II",
                now
        );

        // -------------------------------------------------------------
        // DIMENSIÓN 3: Infraestructura, Laboratorios y Equipamiento
        // -------------------------------------------------------------
        Level1NodeJpaEntity dim3 = Level1NodeJpaEntity.builder()
                .id(UUID.randomUUID())
                .process(process)
                .name("Area 3: Infraestructura, Laboratorios y Equipamiento")
                .order(3)
                .description("Capacidad tecnológica instalada, laboratorios de cómputo y licencias de software.")
                .status(PhaseState.ABIERTA.name())
                .createdAt(now)
                .updatedAt(now)
                .level2Nodes(new ArrayList<>())
                .build();
        level1Repository.save(dim3);

        Level2NodeJpaEntity comp3_1 = Level2NodeJpaEntity.builder()
                .id(UUID.randomUUID())
                .level1Node(dim3)
                .name("Componente 3.1: Entornos de Practica y Computo")
                .order(1)
                .description("Aulas equipadas y laboratorios dedicados de investigación y desarrollo.")
                .createdAt(now)
                .updatedAt(now)
                .level3Nodes(new ArrayList<>())
                .build();
        level2Repository.save(comp3_1);

        Level3NodeJpaEntity crit3_1_1 = Level3NodeJpaEntity.builder()
                .id(UUID.randomUUID())
                .level2Node(comp3_1)
                .name("Criterio 3.1.1: Equipamiento y Licenciamiento Academico")
                .order(1)
                .description("Mantenimiento, actualización de hardware y convenios de software.")
                .createdAt(now)
                .updatedAt(now)
                .indicators(new ArrayList<>())
                .build();
        level3Repository.save(crit3_1_1);

        NormativeIndicatorJpaEntity ind3_1_1 = createIndicator(
                crit3_1_1,
                "IND-3.1.1",
                "Inventario Certificado de Equipos de Laboratorios de Computo",
                1,
                IndicatorState.APROBADO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind3_1_1.getId(),
                coordinatorId,
                "Inventario_Equipos_Laboratorio_Sistemas.pdf",
                "SIGESA-20260515-c5e4f7a8",
                "Acta de inventario físico y operativo de 120 estaciones de trabajo",
                now
        );

        NormativeIndicatorJpaEntity ind3_1_2 = createIndicator(
                crit3_1_1,
                "IND-3.1.2",
                "Licencias de Software y Plataformas para Docencia e Investigacion",
                2,
                IndicatorState.SUBIDO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind3_1_2.getId(),
                coordinatorId,
                "Certificado_Licencias_Software_Academico.pdf",
                "SIGESA-20260520-d6f5a8b9",
                "Convenios vigentes con licencias académicas institucionales",
                now
        );

        log.info("Dossier showcase data creado exitosamente con 3 Dimensiones, 6 Indicadores y 6 DocumentAssets.");
    }

    private NormativeIndicatorJpaEntity createIndicator(
            Level3NodeJpaEntity crit,
            String code,
            String description,
            int order,
            String status,
            LocalDateTime now) {
        NormativeIndicatorJpaEntity indicator = NormativeIndicatorJpaEntity.builder()
                .id(UUID.randomUUID())
                .level3Node(crit)
                .code(code)
                .description(description)
                .order(order)
                .weight(BigDecimal.ONE)
                .referenceUrl("https://duea.umss.edu.bo/normativa/" + code.toLowerCase())
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return indicatorRepository.save(indicator);
    }

    private void createAndLinkDocument(
            UUID processId,
            UUID indicatorId,
            UUID uploadedBy,
            String filename,
            String folioCode,
            String title,
            LocalDateTime now) {
        UUID evidenceId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        byte[] pdfContent = createSamplePdf(title, folioCode, filename);
        String contentHash = computeSha256(pdfContent);
        String storageKey = "dossier_demo_" + folioCode.toLowerCase() + "_" + filename;

        try {
            blobStorage.restore(storageKey, pdfContent);
        } catch (Exception ex) {
            log.warn("No se pudo restaurar blob en almacenamiento local: {}", ex.getMessage());
        }

        EvidenceEntity evidence = new EvidenceEntity();
        evidence.setId(evidenceId);
        evidence.setIndicatorId(indicatorId);
        evidence.setNormativeIndicatorId(indicatorId);
        evidence.setCreatedAt(now);
        evidenceRepository.save(evidence);

        DocumentAssetEntity asset = new DocumentAssetEntity();
        asset.setId(assetId);
        asset.setProcessId(processId);
        asset.setFolioCode(folioCode);
        asset.setTitle(title);
        asset.setStorageKey(storageKey);
        asset.setContentHash(contentHash);
        asset.setInstitutional(false);
        asset.setUploadedBy(uploadedBy);
        asset.setCreatedAt(now);
        assetRepository.save(asset);

        IndicatorEvidenceLinkEntity link = new IndicatorEvidenceLinkEntity();
        link.setId(UUID.randomUUID());
        link.setIndicatorId(indicatorId);
        link.setDocumentAssetId(assetId);
        link.setEvidenceId(evidenceId);
        link.setPrimaryReference(true);
        link.setCreatedAt(now);
        linkRepository.save(link);
    }

    private byte[] createSamplePdf(String title, String folio, String filename) {
        String cleanTitle = title.replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ .,:_-]", "");
        String streamContent = "BT /F1 16 Tf 50 720 Td (UNIVERSIDAD MAYOR DE SAN SIMON) Tj\n"
                + "/F1 12 Tf 0 -30 Td (SIGESA - Expediente Digital de Acreditacion) Tj\n"
                + "0 -25 Td (Folio Oficial: " + folio + ") Tj\n"
                + "0 -25 Td (Archivo: " + filename + ") Tj\n"
                + "0 -25 Td (Documento: " + cleanTitle + ") Tj\n"
                + "0 -40 Td (Certificacion: Evidencia verificada para el comite de evaluadores externos.) Tj ET";

        String pdf = "%PDF-1.4\n"
                + "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n"
                + "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n"
                + "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj\n"
                + "4 0 obj << /Length " + streamContent.getBytes(StandardCharsets.UTF_8).length + " >>\n"
                + "stream\n"
                + streamContent + "\n"
                + "endstream\n"
                + "endobj\n"
                + "5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n"
                + "xref\n"
                + "0 6\n"
                + "0000000000 65535 f \n"
                + "0000000010 00000 n \n"
                + "0000000060 00000 n \n"
                + "0000000117 00000 n \n"
                + "0000000244 00000 n \n"
                + "0000000390 00000 n \n"
                + "trailer << /Size 6 /Root 1 0 R >>\n"
                + "startxref\n"
                + "480\n"
                + "%%EOF\n";

        return pdf.getBytes(StandardCharsets.UTF_8);
    }

    private String computeSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(data));
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }
}
