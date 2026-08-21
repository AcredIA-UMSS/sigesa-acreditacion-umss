package com.umss.sigesa.application.port.out;

import com.umss.sigesa.adapter.out.persistance.entity.ObservationEntity;
import java.util.List;

public interface ObservationRepositoryPort {
    void save(ObservationEntity observation);
    List<ObservationEntity> findByIndicatorId(String indicatorId);
    void saveAll(List<ObservationEntity> observations);
}
