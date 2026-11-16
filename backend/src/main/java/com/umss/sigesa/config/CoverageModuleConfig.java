package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.GetCoverageMatrixUseCase;
import com.umss.sigesa.application.port.out.CoverageMatrixPdfPort;
import com.umss.sigesa.application.port.out.CoverageMatrixQueryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.coverage.GetCoverageMatrixService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoverageModuleConfig {

    @Bean
    GetCoverageMatrixUseCase getCoverageMatrixUseCase(
            ProcessQueryPort processQueryPort,
            CoverageMatrixQueryPort queryPort,
            CoverageMatrixPdfPort pdfPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new GetCoverageMatrixService(processQueryPort, queryPort, pdfPort, assignmentRepository);
    }
}
