package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateStatus;

import java.util.List;
import java.util.Optional;

public interface ListTemplatesUseCase {

    List<Template> list(Optional<TemplateStatus> status, Optional<String> type);
}
