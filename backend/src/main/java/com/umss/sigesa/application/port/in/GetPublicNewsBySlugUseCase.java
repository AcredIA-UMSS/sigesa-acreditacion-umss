package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.NewsArticle;

public interface GetPublicNewsBySlugUseCase {

    NewsArticle getPublished(String slug);
}
