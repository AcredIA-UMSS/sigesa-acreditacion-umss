package com.umss.sigesa.reports.repository;

import com.umss.sigesa.reports.domain.ReportDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReportRepositoryIntegrationTest {

    @Autowired
    private ReportDefinitionRepository defRepo;

    @Test
    void saveAndFindByCodigo() {
        var def = ReportDefinition.builder()
                .codigo("R1")
                .nombre("Test Report")
                .filtersAllowed(Map.of("careerId","LONG"))
                .metrics(Map.of("m1", Map.of("expression","count(*)")))
                .version(1)
                .build();

        def = defRepo.save(def);
        var found = defRepo.findByCodigoAndDeletedAtIsNull("R1");
        assertThat(found).isPresent();
        assertThat(found.get().getCodigo()).isEqualTo("R1");
    }
}
