package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ObservationEntity;
import com.umss.sigesa.application.port.out.ObservationRepositoryPort;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ObservationJpaAdapter implements ObservationRepositoryPort {

    private final ObservationJpaRepository observationRepository;

    public ObservationJpaAdapter(ObservationJpaRepository observationRepository) {
        this.observationRepository = observationRepository;
    }

    @Override
    public void save(ObservationEntity observation) {
        observationRepository.save(observation);
    }

    @Override
    public List<ObservationEntity> findByIndicatorId(String indicatorId) {
        return observationRepository.findByIndicatorId(indicatorId);
    }

    @Override
    public void saveAll(List<ObservationEntity> observations) {
        observationRepository.saveAll(observations);
    }
}
