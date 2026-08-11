package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.Template;

public interface CreateTemplateUseCase {

    Template create(Template template);
}
