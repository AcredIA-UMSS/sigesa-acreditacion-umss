package com.umss.sigesa.domain.model;

import java.time.Instant;
import java.util.UUID;

public record NewsArticle(
        UUID id,
        String title,
        String slug,
        NewsCategory category,
        String summary,
        String body,
        NewsStatus status,
        Instant publishedAt,
        boolean featured,
        Instant createdAt,
        Instant updatedAt
) {

    public boolean isPublished() {
        return status == NewsStatus.PUBLICADA;
    }

    public boolean hasBody() {
        return body != null && !body.isBlank();
    }

    public NewsArticle withContent(String newTitle,
                                   String newSlug,
                                   NewsCategory newCategory,
                                   String newSummary,
                                   String newBody,
                                   Instant now) {
        return new NewsArticle(
                id, newTitle, newSlug, newCategory, newSummary, newBody,
                status, publishedAt, featured, createdAt, now
        );
    }

    public NewsArticle publish(Instant now) {
        return new NewsArticle(
                id, title, slug, category, summary, body,
                NewsStatus.PUBLICADA, now, featured, createdAt, now
        );
    }

    public NewsArticle unpublish(Instant now) {
        return new NewsArticle(
                id, title, slug, category, summary, body,
                NewsStatus.BORRADOR, null, false, createdAt, now
        );
    }

    public NewsArticle withFeatured(boolean newFeatured, Instant now) {
        return new NewsArticle(
                id, title, slug, category, summary, body,
                status, publishedAt, newFeatured, createdAt, now
        );
    }
}
