package com.umss.sigesa.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.reports.domain.ReportDefinition;
import com.umss.sigesa.reports.domain.ReportRun;
import com.umss.sigesa.reports.repository.ReportDefinitionRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("E2E disabled by default; enable when Testcontainers env is available")
public class ReportsE2ETest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("sigesa_test")
            .withUsername("sa")
            .withPassword("sa");

    @LocalServerPort
    private int port;

    private RestTemplate rest = new RestTemplate();

    @Autowired
    private ReportDefinitionRepository defRepo;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void exportFlow_completes() throws Exception {
        // create a report definition
        ReportDefinition def = ReportDefinition.builder().codigo("E2E1").nombre("E2E Report").metrics(Map.of()).filtersAllowed(Map.of()).version(1).build();
        def = defRepo.save(def);

        // submit export
        ResponseEntity<ReportRun> res = rest.postForEntity("http://localhost:"+port+"/api/v1/reports/"+def.getId()+"/export", Map.of(), ReportRun.class);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        ReportRun run = res.getBody();
        assertThat(run).isNotNull();

        // poll for completion (simple loop)
        for (int i = 0; i < 30; i++) {
            Thread.sleep(1000);
            ResponseEntity<ReportRun> status = rest.getForEntity("http://localhost:"+port+"/api/v1/reports/runs/"+run.getId(), ReportRun.class);
            if (status.getBody() != null && "COMPLETED".equals(status.getBody().getStatus())) {
                assertThat(status.getBody().getDownloadUrl()).isNotBlank();
                return;
            }
        }
        throw new AssertionError("Export did not complete in time");
    }
}
