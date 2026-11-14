package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.PublicNewsDetailResponse;
import com.umss.sigesa.adapter.in.web.dto.PublicNewsSummaryResponse;
import com.umss.sigesa.application.port.in.GetPublicFeaturedNewsUseCase;
import com.umss.sigesa.application.port.in.GetPublicNewsBySlugUseCase;
import com.umss.sigesa.application.port.in.ListPublicNewsUseCase;
import com.umss.sigesa.domain.model.NewsArticle;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/news")
@Tag(name = "public-news", description = "Noticias públicas (anónimo, FSD-UC-035)")
@SecurityRequirements
public class PublicNewsController {

    private final ListPublicNewsUseCase listPublicNewsUseCase;
    private final GetPublicFeaturedNewsUseCase getPublicFeaturedNewsUseCase;
    private final GetPublicNewsBySlugUseCase getPublicNewsBySlugUseCase;

    public PublicNewsController(ListPublicNewsUseCase listPublicNewsUseCase,
                                GetPublicFeaturedNewsUseCase getPublicFeaturedNewsUseCase,
                                GetPublicNewsBySlugUseCase getPublicNewsBySlugUseCase) {
        this.listPublicNewsUseCase = listPublicNewsUseCase;
        this.getPublicFeaturedNewsUseCase = getPublicFeaturedNewsUseCase;
        this.getPublicNewsBySlugUseCase = getPublicNewsBySlugUseCase;
    }

    @GetMapping
    @Operation(operationId = "listPublicNews", summary = "Listar noticias publicadas")
    public Page<PublicNewsSummaryResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);
        return listPublicNewsUseCase.listPublished(
                        PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "publishedAt")))
                .map(PublicNewsController::toSummary);
    }

    @GetMapping("/featured")
    @Operation(operationId = "getPublicFeaturedNews", summary = "Noticia destacada o la más reciente")
    public ResponseEntity<PublicNewsSummaryResponse> featured() {
        return getPublicFeaturedNewsUseCase.featuredOrLatestPublished()
                .map(article -> ResponseEntity.ok(toSummary(article)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{slug}")
    @Operation(operationId = "getPublicNewsBySlug", summary = "Detalle de noticia publicada")
    public PublicNewsDetailResponse get(@PathVariable String slug) {
        NewsArticle article = getPublicNewsBySlugUseCase.getPublished(slug);
        return new PublicNewsDetailResponse(
                article.slug(),
                article.title(),
                article.category().name(),
                article.summary(),
                article.body(),
                article.publishedAt(),
                article.featured()
        );
    }

    private static PublicNewsSummaryResponse toSummary(NewsArticle article) {
        return new PublicNewsSummaryResponse(
                article.slug(),
                article.title(),
                article.category().name(),
                article.summary(),
                article.publishedAt(),
                article.featured()
        );
    }
}
