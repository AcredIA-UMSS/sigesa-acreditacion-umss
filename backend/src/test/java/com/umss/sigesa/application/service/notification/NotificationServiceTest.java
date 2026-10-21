package com.umss.sigesa.application.service.notification;

import com.umss.sigesa.application.model.notification.NotificationItem;
import com.umss.sigesa.application.port.out.NotificationQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private NotificationQueryPort queryPort;
    private NotificationService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID programId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        queryPort = mock(NotificationQueryPort.class);
        service = new NotificationService(queryPort);
    }

    @Test
    @DisplayName("getNotifications: Delega correctamente a NotificationQueryPort")
    void testGetNotifications() {
        NotificationItem item = new NotificationItem(
                UUID.randomUUID(),
                "IndicatorApproved",
                programId,
                "cc@umss.edu.bo",
                "Aprobado",
                "Cuerpo",
                "/link",
                "SENT",
                LocalDateTime.now(),
                null
        );

        when(queryPort.findNotificationsForUser(eq(userId), eq(List.of(programId)), eq("email"), eq(true), any()))
                .thenReturn(new PageImpl<>(List.of(item)));

        var page = service.getNotifications(userId, List.of(programId), "email", true, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        verify(queryPort).findNotificationsForUser(userId, List.of(programId), "email", true, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("getUnreadCount: Retorna cantidad de no leídas delegando al puerto")
    void testGetUnreadCount() {
        when(queryPort.countUnread(userId, List.of(programId), "email")).thenReturn(5L);

        long count = service.getUnreadCount(userId, List.of(programId), "email");

        assertThat(count).isEqualTo(5L);
        verify(queryPort).countUnread(userId, List.of(programId), "email");
    }

    @Test
    @DisplayName("markAsRead y markAllAsRead delegan al puerto")
    void testMarkAsReadDelegation() {
        UUID id = UUID.randomUUID();
        service.markAsRead(id);
        verify(queryPort).markAsRead(id);

        service.markAllAsRead(userId, List.of(programId), "email");
        verify(queryPort).markAllAsRead(userId, List.of(programId), "email");
    }
}
