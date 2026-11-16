package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.entity.NewsArticleJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataNewsArticleRepository;
import com.umss.sigesa.domain.model.NewsCategory;
import com.umss.sigesa.domain.model.NewsStatus;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Noticias de demostración para FSD-UC-035. Tres publicadas (sin destacada; la home usa la más reciente) y un borrador.
 */
@Component
@Profile("!prod")
@Order(91)
public class NewsSeedDataLoader implements ApplicationRunner {

    public static final UUID ID_FEATURED = UUID.fromString("770e8400-e29b-41d4-a716-446655440200");
    public static final UUID ID_TALLER = UUID.fromString("770e8400-e29b-41d4-a716-446655440201");
    public static final UUID ID_LOGRO = UUID.fromString("770e8400-e29b-41d4-a716-446655440202");
    public static final UUID ID_DRAFT = UUID.fromString("770e8400-e29b-41d4-a716-446655440203");

    public static final String SLUG_FEATURED = "facultad-de-medicina-renueva-acreditacion-mercosur";
    public static final String SLUG_TALLER = "taller-de-autoevaluacion-para-nuevas-carreras";
    public static final String SLUG_LOGRO = "reconocimiento-a-la-investigacion-indexada";
    public static final String SLUG_DRAFT = "borrador-interno-duea";

    private final SpringDataNewsArticleRepository repository;

    public NewsSeedDataLoader(SpringDataNewsArticleRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Instant t1 = LocalDateTime.of(2026, 10, 1, 12, 0).toInstant(ZoneOffset.UTC);
        Instant t2 = LocalDateTime.of(2026, 10, 15, 12, 0).toInstant(ZoneOffset.UTC);
        Instant t3 = LocalDateTime.of(2026, 10, 20, 12, 0).toInstant(ZoneOffset.UTC);

        seed(ID_FEATURED, "Facultad de Medicina renueva Acreditación MERCOSUR", SLUG_FEATURED,
                NewsCategory.EVENTO,
                "Tras un riguroso proceso de evaluación, la carrera de medicina mantiene sus estándares de excelencia regional.",
                "La Facultad de Medicina de la UMSS renovó su acreditación MERCOSUR.\n\nEl proceso confirmó los estándares de excelencia regional.",
                NewsStatus.PUBLICADA, t1, false);
        seed(ID_TALLER, "Taller de Autoevaluación para nuevas carreras", SLUG_TALLER,
                NewsCategory.CAPACITACION,
                "Iniciamos la formación para los comités encargados de los nuevos procesos 2026.",
                "La DUEA inicia el taller de autoevaluación para comités de nuevas carreras.",
                NewsStatus.PUBLICADA, t2, false);
        seed(ID_LOGRO, "Reconocimiento a la Investigación Indexada", SLUG_LOGRO,
                NewsCategory.LOGRO,
                "La DUEA destaca el incremento de publicaciones en revistas de alto impacto.",
                "Se reconoció el incremento de publicaciones indexadas de alto impacto.",
                NewsStatus.PUBLICADA, t3, false);
        seed(ID_DRAFT, "Borrador interno DUEA", SLUG_DRAFT,
                NewsCategory.EVENTO,
                "Este comunicado aún no está autorizado para el público.",
                "Texto interno que no debe enumerarse en el portal.",
                NewsStatus.BORRADOR, null, false);
    }

    private void seed(UUID id,
                      String title,
                      String slug,
                      NewsCategory category,
                      String summary,
                      String body,
                      NewsStatus status,
                      Instant publishedAt,
                      boolean featured) {
        if (repository.existsById(id)) {
            return;
        }
        Instant now = Instant.parse("2026-10-20T12:00:00Z");
        repository.save(NewsArticleJpaEntity.builder()
                .id(id)
                .title(title)
                .slug(slug)
                .category(category.name())
                .summary(summary)
                .body(body)
                .status(status.name())
                .publishedAt(publishedAt)
                .featured(featured)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }
}
