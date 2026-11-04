package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface NewsArticlePort {

    NewsArticle save(NewsArticle article);

    Optional<NewsArticle> findById(UUID id);

    Optional<NewsArticle> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Optional<NewsArticle> findPublishedFeatured();

    Optional<NewsArticle> findLatestPublished();

    Page<NewsArticle> findPublished(Pageable pageable);

    Page<NewsArticle> findAll(Pageable pageable);

    void clearFeaturedExcept(UUID keepId);

    void deleteById(UUID id);
}
