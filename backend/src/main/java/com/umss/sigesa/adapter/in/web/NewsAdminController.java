package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.AdminNewsResponse;
import com.umss.sigesa.adapter.in.web.dto.NewsWriteRequest;
import com.umss.sigesa.application.port.in.CreateNewsUseCase;
import com.umss.sigesa.application.port.in.DeleteNewsUseCase;
import com.umss.sigesa.application.port.in.FeatureNewsUseCase;
import com.umss.sigesa.application.port.in.GetAdminNewsUseCase;
import com.umss.sigesa.application.port.in.ListAdminNewsUseCase;
import com.umss.sigesa.application.port.in.PublishNewsUseCase;
import com.umss.sigesa.application.port.in.UnpublishNewsUseCase;
import com.umss.sigesa.application.port.in.UpdateNewsUseCase;
import com.umss.sigesa.domain.exception.ValidationException;
import com.umss.sigesa.domain.model.NewsArticle;
import com.umss.sigesa.domain.model.NewsCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/news")
@Tag(name = "admin-news", description = "Administración de noticias [JD] (FSD-UC-035)")
public class NewsAdminController {

    private final ListAdminNewsUseCase listAdminNewsUseCase;
    private final GetAdminNewsUseCase getAdminNewsUseCase;
    private final CreateNewsUseCase createNewsUseCase;
    private final UpdateNewsUseCase updateNewsUseCase;
    private final DeleteNewsUseCase deleteNewsUseCase;
    private final PublishNewsUseCase publishNewsUseCase;
    private final UnpublishNewsUseCase unpublishNewsUseCase;
    private final FeatureNewsUseCase featureNewsUseCase;

    public NewsAdminController(ListAdminNewsUseCase listAdminNewsUseCase,
                               GetAdminNewsUseCase getAdminNewsUseCase,
                               CreateNewsUseCase createNewsUseCase,
                               UpdateNewsUseCase updateNewsUseCase,
                               DeleteNewsUseCase deleteNewsUseCase,
                               PublishNewsUseCase publishNewsUseCase,
                               UnpublishNewsUseCase unpublishNewsUseCase,
                               FeatureNewsUseCase featureNewsUseCase) {
        this.listAdminNewsUseCase = listAdminNewsUseCase;
        this.getAdminNewsUseCase = getAdminNewsUseCase;
        this.createNewsUseCase = createNewsUseCase;
        this.updateNewsUseCase = updateNewsUseCase;
        this.deleteNewsUseCase = deleteNewsUseCase;
        this.publishNewsUseCase = publishNewsUseCase;
        this.unpublishNewsUseCase = unpublishNewsUseCase;
        this.featureNewsUseCase = featureNewsUseCase;
    }

    @GetMapping
    @Operation(operationId = "listAdminNews", summary = "Listar todas las noticias")
    public Page<AdminNewsResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        return listAdminNewsUseCase.listAll(
                        PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt")))
                .map(NewsAdminController::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getAdminNews", summary = "Detalle admin de una noticia")
    public AdminNewsResponse get(@PathVariable UUID id) {
        return toResponse(getAdminNewsUseCase.getById(id));
    }

    @PostMapping
    @Operation(operationId = "createNews", summary = "Crear noticia en borrador")
    public ResponseEntity<AdminNewsResponse> create(@Valid @RequestBody NewsWriteRequest request) {
        NewsArticle created = createNewsUseCase.create(new CreateNewsUseCase.CreateNewsCommand(
                request.title(),
                parseCategory(request.category()),
                request.summary(),
                request.body()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateNews", summary = "Editar contenido (no cambia status)")
    public AdminNewsResponse update(@PathVariable UUID id, @Valid @RequestBody NewsWriteRequest request) {
        return toResponse(updateNewsUseCase.update(new UpdateNewsUseCase.UpdateNewsCommand(
                id,
                request.title(),
                parseCategory(request.category()),
                request.summary(),
                request.body()
        )));
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteNews", summary = "Eliminar solo si está en borrador")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteNewsUseCase.deleteDraft(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    @Operation(operationId = "publishNews", summary = "Publicar noticia")
    public AdminNewsResponse publish(@PathVariable UUID id) {
        return toResponse(publishNewsUseCase.publish(id));
    }

    @PostMapping("/{id}/unpublish")
    @Operation(operationId = "unpublishNews", summary = "Despublicar noticia")
    public AdminNewsResponse unpublish(@PathVariable UUID id) {
        return toResponse(unpublishNewsUseCase.unpublish(id));
    }

    @PostMapping("/{id}/feature")
    @Operation(operationId = "featureNews", summary = "Marcar destacada (solo publicada)")
    public AdminNewsResponse feature(@PathVariable UUID id) {
        return toResponse(featureNewsUseCase.feature(id));
    }

    private static NewsCategory parseCategory(String raw) {
        try {
            return NewsCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new ValidationException("La categoría debe ser EVENTO, CAPACITACION o LOGRO.");
        }
    }

    private static AdminNewsResponse toResponse(NewsArticle article) {
        return new AdminNewsResponse(
                article.id(),
                article.slug(),
                article.title(),
                article.category().name(),
                article.summary(),
                article.body(),
                article.status().name(),
                article.publishedAt(),
                article.featured()
        );
    }
}
