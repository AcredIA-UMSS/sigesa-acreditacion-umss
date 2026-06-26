package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.application.port.out.AccreditationProcessRepositoryPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessType;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class AccreditationProcessJpaAdapter implements AccreditationProcessRepositoryPort {

    // private final AccreditationProcessSpringDataRepository jpaRepository;

    @Override
    public boolean existsActiveProcessByCareerAndTypeAndPeriod(UUID careerId, ProcessType type, String period) {
        // TODO: Hacer la consulta real a la base de datos
        return false;
    }

    @Override
    public AccreditationProcess save(AccreditationProcess process) {
        // TODO: Mapear el 'process' de dominio a entidad JPA y guardarlo en BD
        return process;
    }
}