package com.umss.sigesa.application.port.in;

import java.util.List;

public interface ListPublicProgramsUseCase {

    record PublicProgramSummary(String slug, String name) {}

    List<PublicProgramSummary> listPublished();
}
