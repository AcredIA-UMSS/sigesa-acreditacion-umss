package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListPublicNewsUseCase {

    Page<NewsArticle> listPublished(Pageable pageable);
}
