package com.umss.sigesa.config;

import com.umss.sigesa.application.port.out.PublicCertificatePdfRendererPort;
import com.umss.sigesa.application.port.out.PublicProgramQueryPort;
import com.umss.sigesa.application.service.publicportal.PublicPortalService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PublicPortalModuleConfig {

    @Bean
    PublicPortalService publicPortalService(PublicProgramQueryPort queryPort,
                                            PublicCertificatePdfRendererPort pdfRenderer) {
        return new PublicPortalService(queryPort, pdfRenderer);
    }
}
