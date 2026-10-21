package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.NotificationResponseDto;
import com.umss.sigesa.adapter.in.web.dto.NotificationUnreadCountDto;
import com.umss.sigesa.application.model.notification.NotificationItem;
import com.umss.sigesa.application.port.in.GetNotificationsUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    private GetNotificationsUseCase getNotificationsUseCase;
    private UserProgramAssignmentRepositoryPort assignmentRepositoryPort;
    private NotificationController controller;

    private final UUID userId = UUID.randomUUID();
    private final UUID programId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        getNotificationsUseCase = mock(GetNotificationsUseCase.class);
        assignmentRepositoryPort = mock(UserProgramAssignmentRepositoryPort.class);
        controller = new NotificationController(getNotificationsUseCase, assignmentRepositoryPort);

        var auth = new UsernamePasswordAuthenticationToken(userId, null, List.of());
        auth.setDetails(List.of(programId));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("getMyNotifications: Retorna página con notificaciones para el usuario")
    void testGetMyNotifications() {
        NotificationItem item = new NotificationItem(
                UUID.randomUUID(),
                "IndicatorRejected",
                programId,
                "cc@umss.edu.bo",
                "Indicador Rechazado",
                "Se ha observado la evidencia",
                "/procesos/123?indicadorId=456",
                "PENDING",
                LocalDateTime.now(),
                null
        );

        when(getNotificationsUseCase.getNotifications(eq(userId), anyList(), anyString(), eq(false), any()))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        var response = controller.getMyNotifications(false, PageRequest.of(0, 20));

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).subject()).isEqualTo("Indicador Rechazado");
    }

    @Test
    @DisplayName("getUnreadCount: Retorna conteo de notificaciones no leídas")
    void testGetUnreadCount() {
        when(getNotificationsUseCase.getUnreadCount(eq(userId), anyList(), anyString()))
                .thenReturn(3L);

        ResponseEntity<NotificationUnreadCountDto> response = controller.getUnreadCount();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().unreadCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("markAsRead: Invoca caso de uso para marcar notificación específica")
    void testMarkAsRead() {
        UUID notifId = UUID.randomUUID();
        doNothing().when(getNotificationsUseCase).markAsRead(notifId);

        var response = controller.markAsRead(notifId);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(getNotificationsUseCase).markAsRead(notifId);
    }

    @Test
    @DisplayName("markAllAsRead: Invoca caso de uso para marcar todas las notificaciones")
    void testMarkAllAsRead() {
        doNothing().when(getNotificationsUseCase).markAllAsRead(eq(userId), anyList(), anyString());

        var response = controller.markAllAsRead();

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(getNotificationsUseCase).markAllAsRead(eq(userId), anyList(), anyString());
    }
}
