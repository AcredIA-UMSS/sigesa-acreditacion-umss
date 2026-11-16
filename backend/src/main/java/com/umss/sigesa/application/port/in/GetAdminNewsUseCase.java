package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.NewsArticle;

import java.util.UUID;

public interface GetAdminNewsUseCase {

    NewsArticle getById(UUID id);
}
