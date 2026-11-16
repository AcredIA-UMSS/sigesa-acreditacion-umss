package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.NewsArticleJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataNewsArticleRepository extends JpaRepository<NewsArticleJpaEntity, UUID> {

    Optional<NewsArticleJpaEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Optional<NewsArticleJpaEntity> findFirstByStatusAndFeaturedTrue(String status);

    Optional<NewsArticleJpaEntity> findFirstByStatusOrderByPublishedAtDesc(String status);

    Page<NewsArticleJpaEntity> findByStatus(String status, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update NewsArticleJpaEntity n set n.featured = false where n.featured = true and n.id <> :keepId")
    void clearFeaturedExcept(@Param("keepId") UUID keepId);
}
