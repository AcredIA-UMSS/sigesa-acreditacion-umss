package com.umss.sigesa.adapter.out.notification;

import com.umss.sigesa.adapter.out.persistance.NotificationOutboxJpaRepository;
import com.umss.sigesa.domain.model.NotificationModuleSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InProcessNotificationModuleAdapter — E3")
class InProcessNotificationModuleAdapterTest {

    @Mock
    private NotificationOutboxJpaRepository outboxRepository;

    @Test
    @DisplayName("Lee el outbox local sin red")
    void inspect_readsLocalOutbox() {
        when(outboxRepository.countByStatus("PENDING")).thenReturn(4L);
        when(outboxRepository.count()).thenReturn(9L);

        NotificationModuleSnapshot snapshot =
                new InProcessNotificationModuleAdapter(outboxRepository).inspect(Duration.ofMillis(800));

        assertEquals(4L, snapshot.pendingCount());
        assertEquals(9L, snapshot.totalCount());
        assertEquals("in_process", snapshot.origin());
    }
}
