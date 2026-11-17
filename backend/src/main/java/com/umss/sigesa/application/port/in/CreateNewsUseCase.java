package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.NewsArticle;
import com.umss.sigesa.domain.model.NewsCategory;

public interface CreateNewsUseCase {

    record CreateNewsCommand(
            String title,
            NewsCategory category,
            String summary,
            String body
    ) {}

    NewsArticle create(CreateNewsCommand command);
}
