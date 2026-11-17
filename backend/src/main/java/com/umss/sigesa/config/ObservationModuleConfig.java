package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.EnqueueDomainNotificationUseCase;
import com.umss.sigesa.application.port.in.ProcessObservationUseCase;
import com.umss.sigesa.application.port.out.EvidenceLookupPort;
import com.umss.sigesa.application.port.out.ProcessObservationRepositoryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.service.observation.ProcessObservationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ObservationModuleConfig {

    @Bean
    ProcessObservationUseCase processObservationUseCase(ProcessQueryPort processQueryPort,
                                                        EvidenceLookupPort evidenceLookupPort,
                                                        ProcessObservationRepositoryPort observationRepository,
                                                        EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase) {
        return new ProcessObservationService(
                processQueryPort,
                evidenceLookupPort,
                observationRepository,
                enqueueDomainNotificationUseCase,
                Clock.systemUTC());
    }
}
