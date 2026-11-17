package com.umss.sigesa.adapter.out.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;

class DefaultNotificationSenderAdapterTest {

    @Test
    @DisplayName("Log-only fallback send does not throw")
    void sendDoesNotThrow() {
        DefaultNotificationSenderAdapter adapter = new DefaultNotificationSenderAdapter();
        assertThatCode(() -> adapter.send(
                UUID.randomUUID(),
                "cc@umss.edu.bo",
                "Asunto",
                "Cuerpo",
                "/executive/semaphore?processId=" + UUID.randomUUID()))
                .doesNotThrowAnyException();
    }
}
