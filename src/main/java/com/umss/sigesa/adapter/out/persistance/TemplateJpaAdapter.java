package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.application.port.out.TemplateRepositoryPort;
import com.umss.sigesa.domain.model.Template;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository // ¡Esta anotación es la que soluciona el error! Le dice a Spring que registre este Bean.
public class TemplateJpaAdapter implements TemplateRepositoryPort {

    // Aquí en el futuro inyectarás tu interfaz de Spring Data JPA:
    // private final TemplateSpringDataRepository jpaRepository;

    @Override
    public Optional<Template> findById(UUID id) {
        // TODO: Implementar la búsqueda real en base de datos usando Spring Data
        // y mapear la entidad JPA (@Entity) a la entidad de Dominio (Template).
        return Optional.empty();
    }
}
