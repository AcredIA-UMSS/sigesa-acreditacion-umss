package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.SearchNormativeDocumentsUseCase;
import com.umss.sigesa.application.port.out.NormativeDocumentSearchPort;
import com.umss.sigesa.application.service.normative.SearchNormativeDocumentsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NormativeModuleConfig {

    @Bean
    SearchNormativeDocumentsUseCase searchNormativeDocumentsUseCase(
            NormativeDocumentSearchPort normativeDocumentSearchPort) {
        return new SearchNormativeDocumentsService(normativeDocumentSearchPort);
    }
}
