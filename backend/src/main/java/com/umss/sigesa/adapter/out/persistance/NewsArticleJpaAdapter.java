package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.NewsArticleJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataNewsArticleRepository;
import com.umss.sigesa.application.port.out.NewsArticlePort;
import com.umss.sigesa.domain.model.NewsArticle;
import com.umss.sigesa.domain.model.NewsCategory;
import com.umss.sigesa.domain.model.NewsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public class NewsArticleJpaAdapter implements NewsArticlePort {

    private final SpringDataNewsArticleRepository repository;

    public NewsArticleJpaAdapter(SpringDataNewsArticleRepository repository) {
        this.repository = repository;
    }

    @Override
    public NewsArticle save(NewsArticle article) {
        return toDomain(repository.save(toEntity(article)));
    }

    @Override
    public Optional<NewsArticle> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<NewsArticle> findBySlug(String slug) {
        return repository.findBySlug(slug).map(this::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return repository.existsBySlug(slug);
    }

    @Override
    public Optional<NewsArticle> findPublishedFeatured() {
        return repository.findFirstByStatusAndFeaturedTrue(NewsStatus.PUBLICADA.name()).map(this::toDomain);
    }

    @Override
    public Optional<NewsArticle> findLatestPublished() {
        return repository.findFirstByStatusOrderByPublishedAtDesc(NewsStatus.PUBLICADA.name()).map(this::toDomain);
    }

    @Override
    public Page<NewsArticle> findPublished(Pageable pageable) {
        return repository.findByStatus(NewsStatus.PUBLICADA.name(), pageable).map(this::toDomain);
    }

    @Override
    public Page<NewsArticle> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDomain);
    }

    @Override
    @Transactional
    public void clearFeaturedExcept(UUID keepId) {
        repository.clearFeaturedExcept(keepId);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private NewsArticle toDomain(NewsArticleJpaEntity entity) {
        return new NewsArticle(
                entity.getId(),
                entity.getTitle(),
                entity.getSlug(),
                NewsCategory.valueOf(entity.getCategory()),
                entity.getSummary(),
                entity.getBody(),
                NewsStatus.valueOf(entity.getStatus()),
                entity.getPublishedAt(),
                entity.isFeatured(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static NewsArticleJpaEntity toEntity(NewsArticle article) {
        return NewsArticleJpaEntity.builder()
                .id(article.id())
                .title(article.title())
                .slug(article.slug())
                .category(article.category().name())
                .summary(article.summary())
                .body(article.body())
                .status(article.status().name())
                .publishedAt(article.publishedAt())
                .featured(article.featured())
                .createdAt(article.createdAt())
                .updatedAt(article.updatedAt())
                .build();
    }
}
