package com.umss.sigesa.config;

import com.umss.sigesa.adapter.out.persistance.AppUserJpaRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataLevel1NodeRepository;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class DossierDemoDataLoaderTest {

    @Autowired
    private SpringDataAccreditationProcessRepository processRepository;
    @Autowired
    private SpringDataLevel1NodeRepository level1Repository;
    @Autowired
    private AppUserJpaRepository userRepository;
    @Autowired
    private EvidenceBlobStoragePort blobStorage;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void shouldSeedDossierDataWithoutException() {
        DossierDemoDataLoader loader = new DossierDemoDataLoader(
                processRepository,
                level1Repository,
                userRepository,
                blobStorage,
                entityManager,
                transactionManager
        );

        loader.run(new DefaultApplicationArguments());

        assertTrue(level1Repository.existsByProcessId(DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE),
                "Level 1 nodes should exist after loader run");
    }
}
