package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.AppUserJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.AppUserEntity;
import com.umss.sigesa.adapter.out.persistance.entity.DocumentAssetEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEvidenceLinkEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level1NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level2NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level3NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.NormativeIndicatorJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataLevel1NodeRepository;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.PhaseState;
import jakarta.persistence.EntityManager;
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
    private final AppUserJpaRepository userRepository;
    private final EvidenceBlobStoragePort blobStorage;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    public DossierDemoDataLoader(
            SpringDataAccreditationProcessRepository processRepository,
            SpringDataLevel1NodeRepository level1Repository,
            AppUserJpaRepository userRepository,
            EvidenceBlobStoragePort blobStorage,
            EntityManager entityManager,
            PlatformTransactionManager transactionManager) {
        this.processRepository = processRepository;
        this.level1Repository = level1Repository;
        this.userRepository = userRepository;
        this.blobStorage = blobStorage;
        this.entityManager = entityManager;
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
                log.warn("Dossier showcase seeding omitido o fallido: {}", ex.getMessage(), ex);
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
            entityManager.createNativeQuery("""
                    INSERT INTO accreditation_processes (id, career_id, template_id, status, start_date, operational_mode)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """)
                    .setParameter(1, processId)
                    .setParameter(2, DevSeedData.PROGRAM_INF_SIS)
                    .setParameter(3, DevSeedData.TEMPLATE_CEUB_2026)
                    .setParameter(4, "ACTIVE")
                    .setParameter(5, LocalDateTime.now().minusMonths(3))
                    .setParameter(6, "ACTIVE")
                    .executeUpdate();
            return processRepository.findById(processId).orElseThrow();
        });

        LocalDateTime now = LocalDateTime.now();
        UUID coordinatorId = userRepository.findByEmail(AuthDataLoader.SEED_CC_EMAIL)
                .map(AppUserEntity::getId)
                .orElse(AuthDataLoader.SEED_CC_USER_ID);

        // ================================================================
        // DIMENSION 1: Normas Generales y Definicion Institucional
        // ================================================================
        Level1NodeJpaEntity dim1 = createDimension(
                process,
                "Area 1: Normas Generales y Definicion Institucional",
                1,
                "Marco normativo, estructura institucional y gobierno universitario de la carrera.",
                now
        );

        Level2NodeJpaEntity comp1_1 = createComponent(
                dim1,
                "Componente 1.1: Marco Normativo y Gobierno",
                1,
                "Resoluciones y estatutos constitutivos del programa academico.",
                now
        );

        Level3NodeJpaEntity crit1_1_1 = createCriterion(
                comp1_1,
                "Criterio 1.1.1: Estatuto Organico y Reglamentos",
                1,
                "Existencia y aplicacion de normativa organica formal.",
                now
        );

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
                "Estatuto Organico de la Universidad Mayor de San Simon actualizado al 2025",
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
                "Plan Estrategico Quinquenal de la Facultad de Ciencias y Tecnologia",
                now
        );

        Level3NodeJpaEntity crit1_2_1 = createCriterion(
                comp1_1,
                "Criterio 1.1.2: Reglamento de Regimen Academico",
                2,
                "Normas que rigen la vida academica del estudiantado.",
                now
        );

        NormativeIndicatorJpaEntity ind1_2_1 = createIndicator(
                crit1_2_1,
                "IND-1.2.1",
                "Reglamento de Regimen Academico y de Evaluacion de Aprendizajes",
                1,
                IndicatorState.APROBADO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind1_2_1.getId(),
                coordinatorId,
                "Reglamento_Regimen_Academico_2025.pdf",
                "SIGESA-20260320-a1b2c3d4",
                "Normativa vigente para regimen academico y evaluacion",
                now
        );

        // ================================================================
        // DIMENSION 2: Proyecto Curricular y Plan de Estudios
        // ================================================================
        Level1NodeJpaEntity dim2 = createDimension(
                process,
                "Area 2: Proyecto Curricular y Plan de Estudios",
                2,
                "Disenno y actualizacion permanente del currículo y asignaturas de Ingenieria de Sistemas.",
                now
        );

        Level2NodeJpaEntity comp2_1 = createComponent(
                dim2,
                "Componente 2.1: Diseno Curricular y Perfil Profesional",
                1,
                "Malla curricular y congruencia con el mercado profesional de software.",
                now
        );

        Level3NodeJpaEntity crit2_1_1 = createCriterion(
                comp2_1,
                "Criterio 2.1.1: Malla Curricular y Planes Globales",
                1,
                "Estructura de materias troncales, electivas y pre-requisitos.",
                now
        );

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
                "Malla Curricular oficial aprobada por Resolucion HCU 84/2024",
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
                "Planes Globales de las asignaturas de Taller de Ingenieria de Software I y II",
                now
        );

        Level3NodeJpaEntity crit2_2_1 = createCriterion(
                comp2_1,
                "Criterio 2.2.1: Perfil del Egreso y Competencias",
                2,
                "Definicion del perfil profesional y competencias del egresado.",
                now
        );

        NormativeIndicatorJpaEntity ind2_2_1 = createIndicator(
                crit2_2_1,
                "IND-2.2.1",
                "Perfil Profesional del Egresado de Ingenieria de Sistemas",
                1,
                IndicatorState.APROBADO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind2_2_1.getId(),
                coordinatorId,
                "Perfil_Egresado_Ing_Sistemas.pdf",
                "SIGESA-20260415-c5d6e7f8",
                "Perfil de egreso aprobado por el Consejo Directivo",
                now
        );

        // ================================================================
        // DIMENSION 3: Infraestructura, Laboratorios y Equipamiento
        // ================================================================
        Level1NodeJpaEntity dim3 = createDimension(
                process,
                "Area 3: Infraestructura, Laboratorios y Equipamiento",
                3,
                "Capacidad tecnologica instalada, laboratorios de computo y licencias de software.",
                now
        );

        Level2NodeJpaEntity comp3_1 = createComponent(
                dim3,
                "Componente 3.1: Entornos de Practica y Computo",
                1,
                "Aulas equipadas y laboratorios dedicados de investigacion y desarrollo.",
                now
        );

        Level3NodeJpaEntity crit3_1_1 = createCriterion(
                comp3_1,
                "Criterio 3.1.1: Equipamiento y Licenciamiento Academico",
                1,
                "Mantenimiento, actualizacion de hardware y convenios de software.",
                now
        );

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
                "Acta de inventario fisico y operativo de 120 estaciones de trabajo",
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
                "Convenios vigentes con licencias academicas institucionales",
                now
        );

        Level3NodeJpaEntity crit3_2_1 = createCriterion(
                comp3_1,
                "Criterio 3.2.1: Conectividad y Servicios de Red",
                2,
                "Infraestructura de red, ancho de banda y servicios digitales.",
                now
        );

        NormativeIndicatorJpaEntity ind3_2_1 = createIndicator(
                crit3_2_1,
                "IND-3.2.1",
                "Informe de Ancho de Banda y Calidad de Servicio Internet",
                1,
                IndicatorState.OBSERVADO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind3_2_1.getId(),
                coordinatorId,
                "Informe_Conectividad_Red_2026.pdf",
                "SIGESA-20260525-e7f8a9b0",
                "Mediciones de ancho de banda y disponibilidad de red",
                now
        );

        // ================================================================
        // DIMENSION 4: Docencia y Calidad Academica
        // ================================================================
        Level1NodeJpaEntity dim4 = createDimension(
                process,
                "Area 4: Docencia y Calidad Academica",
                4,
                "Procesos ensenanza-aprendizaje, evaluacion y mejoramiento continuo de la docencia.",
                now
        );

        Level2NodeJpaEntity comp4_1 = createComponent(
                dim4,
                "Componente 4.1: Procesos Docentes y Evaluativos",
                1,
                "Planeacion, ejecucion y evaluacion de la activity docente.",
                now
        );

        Level3NodeJpaEntity crit4_1_1 = createCriterion(
                comp4_1,
                "Criterio 4.1.1: Planificacion Docente y Programas de Asignatura",
                1,
                "Programas de asignatura actualizados y planeaciones de clase.",
                now
        );

        NormativeIndicatorJpaEntity ind4_1_1 = createIndicator(
                crit4_1_1,
                "IND-4.1.1",
                "Programas de Asignatura de Materias Troncales",
                1,
                IndicatorState.APROBADO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind4_1_1.getId(),
                coordinatorId,
                "Programas_Asignatura_Troncales.pdf",
                "SIGESA-20260601-f8a9b0c1",
                "Programas de asignatura de las 15 materias troncales del plan",
                now
        );

        NormativeIndicatorJpaEntity ind4_1_2 = createIndicator(
                crit4_1_1,
                "IND-4.1.2",
                "Cronograma de Evaluaciones y Parciales del Semestre",
                2,
                IndicatorState.SUBIDO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind4_1_2.getId(),
                coordinatorId,
                "Cronograma_Evaluaciones_2026-1.pdf",
                "SIGESA-20260605-a9b0c1d2",
                "Fechas de parciales, recuperaciones y examenes finales",
                now
        );

        Level3NodeJpaEntity crit4_2_1 = createCriterion(
                comp4_1,
                "Criterio 4.2.1: Registro de Notas y Actas",
                2,
                "Procedimientos de registro, calificacion y actas de examen.",
                now
        );

        NormativeIndicatorJpaEntity ind4_2_1 = createIndicator(
                crit4_2_1,
                "IND-4.2.1",
                "Procedimiento de Registro de Notas y Actas de Examen",
                1,
                IndicatorState.APROBADO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind4_2_1.getId(),
                coordinatorId,
                "Procedimiento_Notas_Actas.pdf",
                "SIGESA-20260610-b0c1d2e3",
                "Normas para el registro de notas y actas de examen",
                now
        );

        // ================================================================
        // DIMENSION 5: Investigacion y Extension
        // ================================================================
        Level1NodeJpaEntity dim5 = createDimension(
                process,
                "Area 5: Investigacion y Extension",
                5,
                "Produccion cientifica, proyectos de investigacion y vinculacion con el medio.",
                now
        );

        Level2NodeJpaEntity comp5_1 = createComponent(
                dim5,
                "Componente 5.1: Grupos de Investigacion y Proyectos",
                1,
                "Grupos de investigacion consolidados y proyectos en ejecucion.",
                now
        );

        Level3NodeJpaEntity crit5_1_1 = createCriterion(
                comp5_1,
                "Criterio 5.1.1: Proyectos de Investigacion Activos",
                1,
                "Numero y estado de proyectos de investigacion en curso.",
                now
        );

        NormativeIndicatorJpaEntity ind5_1_1 = createIndicator(
                crit5_1_1,
                "IND-5.1.1",
                "Catalogo de Proyectos de Investigacion 2025-2026",
                1,
                IndicatorState.SUBIDO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind5_1_1.getId(),
                coordinatorId,
                "Proyectos_Investigacion_2025_2026.pdf",
                "SIGESA-20260615-c1d2e3f4",
                "Listado de proyectos de investigacion vigentes con sus responsables",
                now
        );

        NormativeIndicatorJpaEntity ind5_1_2 = createIndicator(
                crit5_1_1,
                "IND-5.1.2",
                "Articulos Publicados en Revistas Indexadas",
                2,
                IndicatorState.APROBADO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind5_1_2.getId(),
                coordinatorId,
                "Articulos_Revistas_Indexadas_2025.pdf",
                "SIGESA-20260620-d2e3f4a5",
                "Articulos publicados en revistas WoS y Scopus por docentes",
                now
        );

        Level3NodeJpaEntity crit5_2_1 = createCriterion(
                comp5_1,
                "Criterio 5.2.1: Proyectos de Extension Universitaria",
                2,
                "Actividades de extension y vinculacion con sectores externos.",
                now
        );

        NormativeIndicatorJpaEntity ind5_2_1 = createIndicator(
                crit5_2_1,
                "IND-5.2.1",
                "Informe de Actividades de Extension 2025",
                1,
                IndicatorState.APROBADO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind5_2_1.getId(),
                coordinatorId,
                "Informe_Extension_Universitaria_2025.pdf",
                "SIGESA-20260625-e3f4a5b6",
                "Actividades de extension realizadas con instituciones externas",
                now
        );

        // ================================================================
        // DIMENSION 6: Gestion y Aseguramiento de Calidad
        // ================================================================
        Level1NodeJpaEntity dim6 = createDimension(
                process,
                "Area 6: Gestion y Aseguramiento de Calidad",
                6,
                "Sistemas de gestion de calidad, autoevaluacion y acreditacion institucional.",
                now
        );

        Level2NodeJpaEntity comp6_1 = createComponent(
                dim6,
                "Componente 6.1: Sistema de Gestion de Calidad",
                1,
                "Procedimientos, indicadores y metodos de aseguramiento de calidad.",
                now
        );

        Level3NodeJpaEntity crit6_1_1 = createCriterion(
                comp6_1,
                "Criterio 6.1.1: Sistema de Gestion de Calidad Documentado",
                1,
                "Manual de calidad, procedimientos y registros del SGC.",
                now
        );

        NormativeIndicatorJpaEntity ind6_1_1 = createIndicator(
                crit6_1_1,
                "IND-6.1.1",
                "Manual del Sistema de Gestion de Calidad",
                1,
                IndicatorState.APROBADO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind6_1_1.getId(),
                coordinatorId,
                "Manual_Sistema_Gestion_Calidad.pdf",
                "SIGESA-20260701-f4a5b6c7",
                "Manual documentado del SGC de la carrera de Ingenieria de Sistemas",
                now
        );

        NormativeIndicatorJpaEntity ind6_1_2 = createIndicator(
                crit6_1_1,
                "IND-6.1.2",
                "Procedimientos Operativos Estandarizados (POE)",
                2,
                IndicatorState.SUBIDO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind6_1_2.getId(),
                coordinatorId,
                "POE_Sistema_Gestion_Calidad.pdf",
                "SIGESA-20260705-a5b6c7d8",
                "Procedimientos operativos para cada proceso del SGC",
                now
        );

        Level3NodeJpaEntity crit6_2_1 = createCriterion(
                comp6_1,
                "Criterio 6.2.1: Autoevaluacion y Plan de Mejoras",
                2,
                "Resultados de autoevaluacion y plan de mejoras derivadas.",
                now
        );

        NormativeIndicatorJpaEntity ind6_2_1 = createIndicator(
                crit6_2_1,
                "IND-6.2.1",
                "Informe de Autoevaluacion Institucional 2026",
                1,
                IndicatorState.APROBADO.name(),
                now
        );
        createAndLinkDocument(
                processId,
                ind6_2_1.getId(),
                coordinatorId,
                "Informe_Autoevaluacion_2026.pdf",
                "SIGESA-20260710-b6c7d8e9",
                "Informe completo de autoevaluacion para acreditacion",
                now
        );

        log.info("Dossier showcase data creado exitosamente con 6 Dimensiones, 18 Indicadores y 18 DocumentAssets.");
    }

    private Level1NodeJpaEntity createDimension(
            AccreditationProcessJpaEntity process,
            String name,
            int order,
            String description,
            LocalDateTime now) {
        Level1NodeJpaEntity dim = Level1NodeJpaEntity.builder()
                .process(process)
                .name(name)
                .order(order)
                .description(description)
                .status(PhaseState.ABIERTA.name())
                .createdAt(now)
                .updatedAt(now)
                .level2Nodes(new ArrayList<>())
                .build();
        process.getLevel1Nodes().add(dim);
        entityManager.persist(dim);
        return dim;
    }

    private Level2NodeJpaEntity createComponent(
            Level1NodeJpaEntity dim,
            String name,
            int order,
            String description,
            LocalDateTime now) {
        Level2NodeJpaEntity comp = Level2NodeJpaEntity.builder()
                .level1Node(dim)
                .name(name)
                .order(order)
                .description(description)
                .createdAt(now)
                .updatedAt(now)
                .level3Nodes(new ArrayList<>())
                .build();
        dim.getLevel2Nodes().add(comp);
        entityManager.persist(comp);
        return comp;
    }

    private Level3NodeJpaEntity createCriterion(
            Level2NodeJpaEntity comp,
            String name,
            int order,
            String description,
            LocalDateTime now) {
        Level3NodeJpaEntity crit = Level3NodeJpaEntity.builder()
                .level2Node(comp)
                .name(name)
                .order(order)
                .description(description)
                .createdAt(now)
                .updatedAt(now)
                .indicators(new ArrayList<>())
                .build();
        comp.getLevel3Nodes().add(crit);
        entityManager.persist(crit);
        return crit;
    }

    private NormativeIndicatorJpaEntity createIndicator(
            Level3NodeJpaEntity crit,
            String code,
            String description,
            int order,
            String status,
            LocalDateTime now) {
        NormativeIndicatorJpaEntity indicator = NormativeIndicatorJpaEntity.builder()
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
        crit.getIndicators().add(indicator);
        entityManager.persist(indicator);
        return indicator;
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
        entityManager.persist(evidence);

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
        entityManager.persist(asset);

        IndicatorEvidenceLinkEntity link = new IndicatorEvidenceLinkEntity();
        link.setId(UUID.randomUUID());
        link.setIndicatorId(indicatorId);
        link.setDocumentAssetId(assetId);
        link.setEvidenceId(evidenceId);
        link.setPrimaryReference(true);
        link.setCreatedAt(now);
        entityManager.persist(link);
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
                + "0000000056 00000 n \n"
                + "0000000112 00000 n \n"
                + "0000000245 00000 n \n"
                + "0000000370 00000 n \n"
                + "trailer << /Size 6 /Root 1 0 R >>\n"
                + "startxref\n"
                + "444\n"
                + "%%EOF";
        return pdf.getBytes(StandardCharsets.UTF_8);
    }

    private String computeSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
