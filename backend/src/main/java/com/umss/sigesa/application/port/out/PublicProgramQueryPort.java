package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.PublicProgramSnapshot;

import java.util.List;
import java.util.Optional;

public interface PublicProgramQueryPort {

    List<PublicProgramSnapshot> findPublished();

    Optional<PublicProgramSnapshot> findBySlug(String slug);
}
