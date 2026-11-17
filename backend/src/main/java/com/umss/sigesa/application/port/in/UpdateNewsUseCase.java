package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.NewsArticle;
import com.umss.sigesa.domain.model.NewsCategory;

import java.util.UUID;

public interface UpdateNewsUseCase {

    record UpdateNewsCommand(
            UUID id,
            String title,
            NewsCategory category,
            String summary,
            String body
    ) {}

    NewsArticle update(UpdateNewsCommand command);
}
