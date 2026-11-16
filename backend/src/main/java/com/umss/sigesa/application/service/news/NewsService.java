package com.umss.sigesa.application.service.news;

import com.umss.sigesa.application.port.in.CreateNewsUseCase;
import com.umss.sigesa.application.port.in.DeleteNewsUseCase;
import com.umss.sigesa.application.port.in.FeatureNewsUseCase;
import com.umss.sigesa.application.port.in.GetAdminNewsUseCase;
import com.umss.sigesa.application.port.in.GetPublicFeaturedNewsUseCase;
import com.umss.sigesa.application.port.in.GetPublicNewsBySlugUseCase;
import com.umss.sigesa.application.port.in.ListAdminNewsUseCase;
import com.umss.sigesa.application.port.in.ListPublicNewsUseCase;
import com.umss.sigesa.application.port.in.PublishNewsUseCase;
import com.umss.sigesa.application.port.in.UnpublishNewsUseCase;
import com.umss.sigesa.application.port.in.UpdateNewsUseCase;
import com.umss.sigesa.application.port.out.NewsArticlePort;
import com.umss.sigesa.domain.exception.NewsNotFoundException;
import com.umss.sigesa.domain.exception.NewsNotPublishedException;
import com.umss.sigesa.domain.exception.NewsPublishedConflictException;
import com.umss.sigesa.domain.exception.PublicResourceNotFoundException;
import com.umss.sigesa.domain.exception.ValidationException;
import com.umss.sigesa.domain.model.NewsArticle;
import com.umss.sigesa.domain.model.NewsCategory;
import com.umss.sigesa.domain.model.NewsContentSanitizer;
import com.umss.sigesa.domain.model.NewsSlug;
import com.umss.sigesa.domain.model.NewsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class NewsService implements
        ListPublicNewsUseCase,
        GetPublicFeaturedNewsUseCase,
        GetPublicNewsBySlugUseCase,
        ListAdminNewsUseCase,
        GetAdminNewsUseCase,
        CreateNewsUseCase,
        UpdateNewsUseCase,
        DeleteNewsUseCase,
        PublishNewsUseCase,
        UnpublishNewsUseCase,
        FeatureNewsUseCase {

    private final NewsArticlePort newsArticlePort;
    private final Clock clock;

    public NewsService(NewsArticlePort newsArticlePort, Clock clock) {
        this.newsArticlePort = newsArticlePort;
        this.clock = clock;
    }

    @Override
    public Page<NewsArticle> listPublished(Pageable pageable) {
        return newsArticlePort.findPublished(pageable);
    }

    @Override
    public Optional<NewsArticle> featuredOrLatestPublished() {
        return newsArticlePort.findPublishedFeatured()
                .or(newsArticlePort::findLatestPublished);
    }

    @Override
    public NewsArticle getPublished(String slug) {
        String normalized = slug == null ? "" : slug.trim().toLowerCase(Locale.ROOT);
        return newsArticlePort.findBySlug(normalized)
                .filter(NewsArticle::isPublished)
                .orElseThrow(PublicResourceNotFoundException::new);
    }

    @Override
    public Page<NewsArticle> listAll(Pageable pageable) {
        return newsArticlePort.findAll(pageable);
    }

    @Override
    public NewsArticle getById(UUID id) {
        return requireArticle(id);
    }

    @Override
    public NewsArticle create(CreateNewsCommand command) {
        Instant now = clock.instant();
        String title = requireText(command.title(), "El título es obligatorio.");
        String summary = requireText(command.summary(), "El resumen es obligatorio.");
        NewsCategory category = requireCategory(command.category());
        String body = NewsContentSanitizer.sanitize(command.body());
        String slug = uniqueSlug(NewsSlug.fromTitle(title), null);
        NewsArticle article = new NewsArticle(
                UUID.randomUUID(),
                title,
                slug,
                category,
                summary,
                body,
                NewsStatus.BORRADOR,
                null,
                false,
                now,
                now
        );
        return newsArticlePort.save(article);
    }

    @Override
    public NewsArticle update(UpdateNewsCommand command) {
        NewsArticle existing = requireArticle(command.id());
        Instant now = clock.instant();
        String title = requireText(command.title(), "El título es obligatorio.");
        String summary = requireText(command.summary(), "El resumen es obligatorio.");
        NewsCategory category = requireCategory(command.category());
        String body = NewsContentSanitizer.sanitize(command.body());
        String slug = existing.slug();
        if (!existing.title().equals(title)) {
            slug = uniqueSlug(NewsSlug.fromTitle(title), existing.id());
        }
        return newsArticlePort.save(existing.withContent(title, slug, category, summary, body, now));
    }

    @Override
    public void deleteDraft(UUID id) {
        NewsArticle existing = requireArticle(id);
        if (existing.isPublished()) {
            throw new NewsPublishedConflictException();
        }
        newsArticlePort.deleteById(id);
    }

    @Override
    public NewsArticle publish(UUID id) {
        NewsArticle existing = requireArticle(id);
        if (!existing.hasBody()) {
            throw new ValidationException("El cuerpo es obligatorio para publicar.");
        }
        if (existing.isPublished()) {
            return existing;
        }
        return newsArticlePort.save(existing.publish(clock.instant()));
    }

    @Override
    public NewsArticle unpublish(UUID id) {
        NewsArticle existing = requireArticle(id);
        if (!existing.isPublished()) {
            return existing;
        }
        return newsArticlePort.save(existing.unpublish(clock.instant()));
    }

    @Override
    public NewsArticle feature(UUID id) {
        NewsArticle existing = requireArticle(id);
        if (!existing.isPublished()) {
            throw new NewsNotPublishedException();
        }
        newsArticlePort.clearFeaturedExcept(existing.id());
        if (existing.featured()) {
            return existing;
        }
        return newsArticlePort.save(existing.withFeatured(true, clock.instant()));
    }

    private NewsArticle requireArticle(UUID id) {
        return newsArticlePort.findById(id).orElseThrow(NewsNotFoundException::new);
    }

    private String uniqueSlug(String base, UUID excludeId) {
        String candidate = base;
        int suffix = 2;
        while (slugTaken(candidate, excludeId)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private boolean slugTaken(String slug, UUID excludeId) {
        return newsArticlePort.findBySlug(slug)
                .filter(article -> excludeId == null || !article.id().equals(excludeId))
                .isPresent();
    }

    private static String requireText(String value, String message) {
        String sanitized = NewsContentSanitizer.sanitize(value);
        if (sanitized.isBlank()) {
            throw new ValidationException(message);
        }
        return sanitized;
    }

    private static NewsCategory requireCategory(NewsCategory category) {
        if (category == null) {
            throw new ValidationException("La categoría es obligatoria.");
        }
        return category;
    }
}
