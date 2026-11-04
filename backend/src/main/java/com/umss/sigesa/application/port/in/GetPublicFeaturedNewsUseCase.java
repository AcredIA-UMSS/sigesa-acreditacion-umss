package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.NewsArticle;

import java.util.Optional;

public interface GetPublicFeaturedNewsUseCase {

    Optional<NewsArticle> featuredOrLatestPublished();
}
