package com.umss.sigesa.application.service.news;

import com.umss.sigesa.application.port.in.CreateNewsUseCase;
import com.umss.sigesa.application.port.out.NewsArticlePort;
import com.umss.sigesa.domain.exception.NewsNotPublishedException;
import com.umss.sigesa.domain.exception.NewsPublishedConflictException;
import com.umss.sigesa.domain.exception.PublicResourceNotFoundException;
import com.umss.sigesa.domain.exception.ValidationException;
import com.umss.sigesa.domain.model.NewsArticle;
import com.umss.sigesa.domain.model.NewsCategory;
import com.umss.sigesa.domain.model.NewsStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-04T12:00:00Z");

    @Mock
    private NewsArticlePort newsArticlePort;

    private NewsService service;

    @BeforeEach
    void setUp() {
        service = new NewsService(newsArticlePort, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createSanitizesHtmlAndStartsAsDraft() {
        when(newsArticlePort.findBySlug(any())).thenReturn(Optional.empty());
        when(newsArticlePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NewsArticle created = service.create(new CreateNewsUseCase.CreateNewsCommand(
                "Taller <script>x</script> DUEA",
                NewsCategory.CAPACITACION,
                "Resumen",
                "<b>negrita</b> y **markdown**"
        ));

        assertThat(created.status()).isEqualTo(NewsStatus.BORRADOR);
        assertThat(created.featured()).isFalse();
        assertThat(created.title()).isEqualTo("Taller x DUEA");
        assertThat(created.body()).isEqualTo("negrita y **markdown**");
        assertThat(created.slug()).isEqualTo("taller-x-duea");
        assertThat(created.publishedAt()).isNull();
    }

    @Test
    void publicGetHidesDraftWithGenericNotFound() {
        when(newsArticlePort.findBySlug("borrador-interno-duea")).thenReturn(Optional.of(draft()));

        assertThatThrownBy(() -> service.getPublished("borrador-interno-duea"))
                .isInstanceOf(PublicResourceNotFoundException.class)
                .hasMessage(PublicResourceNotFoundException.CLIENT_MESSAGE);
    }

    @Test
    void featuredFallsBackToLatestWhenNoneMarked() {
        when(newsArticlePort.findPublishedFeatured()).thenReturn(Optional.empty());
        when(newsArticlePort.findLatestPublished()).thenReturn(Optional.of(published("reciente", false)));

        assertThat(service.featuredOrLatestPublished())
                .isPresent()
                .get()
                .extracting(NewsArticle::slug)
                .isEqualTo("reciente");
    }

    @Test
    void featureDraftThrows409() {
        UUID id = UUID.randomUUID();
        when(newsArticlePort.findById(id)).thenReturn(Optional.of(draft()));

        assertThatThrownBy(() -> service.feature(id))
                .isInstanceOf(NewsNotPublishedException.class);
        verify(newsArticlePort, never()).clearFeaturedExcept(any());
    }

    @Test
    void featureUnmarksPrevious() {
        NewsArticle current = published("nueva", false);
        when(newsArticlePort.findById(current.id())).thenReturn(Optional.of(current));
        when(newsArticlePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NewsArticle featured = service.feature(current.id());

        verify(newsArticlePort).clearFeaturedExcept(current.id());
        assertThat(featured.featured()).isTrue();
    }

    @Test
    void unpublishClearsFeatured() {
        NewsArticle featured = published("destacada", true);
        when(newsArticlePort.findById(featured.id())).thenReturn(Optional.of(featured));
        when(newsArticlePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NewsArticle result = service.unpublish(featured.id());

        assertThat(result.status()).isEqualTo(NewsStatus.BORRADOR);
        assertThat(result.featured()).isFalse();
        assertThat(result.publishedAt()).isNull();
    }

    @Test
    void deletePublishedConflicts() {
        NewsArticle published = published("publicada", false);
        when(newsArticlePort.findById(published.id())).thenReturn(Optional.of(published));

        assertThatThrownBy(() -> service.deleteDraft(published.id()))
                .isInstanceOf(NewsPublishedConflictException.class);
        verify(newsArticlePort, never()).deleteById(any());
    }

    @Test
    void publishWithoutBodyFailsValidation() {
        NewsArticle emptyBody = new NewsArticle(
                UUID.randomUUID(), "Título", "titulo", NewsCategory.EVENTO, "Resumen", "  ",
                NewsStatus.BORRADOR, null, false, NOW, NOW
        );
        when(newsArticlePort.findById(emptyBody.id())).thenReturn(Optional.of(emptyBody));

        assertThatThrownBy(() -> service.publish(emptyBody.id()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("cuerpo");
    }

    @Test
    void listPublishedDelegatesToPort() {
        when(newsArticlePort.findPublished(any())).thenReturn(new PageImpl<>(List.of(published("a", false))));

        assertThat(service.listPublished(PageRequest.of(0, 10)).getContent()).hasSize(1);
    }

    @Test
    void uniqueSlugAppendsSuffix() {
        when(newsArticlePort.findBySlug("taller-duea")).thenReturn(Optional.of(draft()));
        when(newsArticlePort.findBySlug("taller-duea-2")).thenReturn(Optional.empty());
        ArgumentCaptor<NewsArticle> captor = ArgumentCaptor.forClass(NewsArticle.class);
        when(newsArticlePort.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(new CreateNewsUseCase.CreateNewsCommand(
                "Taller DUEA", NewsCategory.EVENTO, "Resumen", "Cuerpo"));

        assertThat(captor.getValue().slug()).isEqualTo("taller-duea-2");
    }

    private static NewsArticle draft() {
        return new NewsArticle(
                UUID.randomUUID(), "Borrador", "borrador-interno-duea", NewsCategory.EVENTO,
                "Resumen interno", "Cuerpo", NewsStatus.BORRADOR, null, false, NOW, NOW
        );
    }

    private static NewsArticle published(String slug, boolean featured) {
        return new NewsArticle(
                UUID.randomUUID(), "Publicada", slug, NewsCategory.LOGRO, "Resumen", "Cuerpo",
                NewsStatus.PUBLICADA, NOW, featured, NOW, NOW
        );
    }
}
