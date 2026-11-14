package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.web.dto.InAppNotificationResponseDto;
import com.umss.sigesa.application.port.in.ListMyInAppNotificationsUseCase;
import com.umss.sigesa.application.port.in.MarkAllInAppNotificationsReadUseCase;
import com.umss.sigesa.application.port.in.MarkInAppNotificationReadUseCase;
import com.umss.sigesa.domain.model.notification.InAppNotification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "In-app dual-channel notifications (API-NOTIF-02..04)")
public class NotificationController {

    private final ListMyInAppNotificationsUseCase listMyInAppNotificationsUseCase;
    private final MarkInAppNotificationReadUseCase markInAppNotificationReadUseCase;
    private final MarkAllInAppNotificationsReadUseCase markAllInAppNotificationsReadUseCase;

    public NotificationController(
            ListMyInAppNotificationsUseCase listMyInAppNotificationsUseCase,
            MarkInAppNotificationReadUseCase markInAppNotificationReadUseCase,
            MarkAllInAppNotificationsReadUseCase markAllInAppNotificationsReadUseCase) {
        this.listMyInAppNotificationsUseCase = listMyInAppNotificationsUseCase;
        this.markInAppNotificationReadUseCase = markInAppNotificationReadUseCase;
        this.markAllInAppNotificationsReadUseCase = markAllInAppNotificationsReadUseCase;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List my in-app notifications")
    public ResponseEntity<Page<InAppNotificationResponseDto>> listMine(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UUID userId = extractUserId(SecurityContextHolder.getContext().getAuthentication());
        Page<InAppNotificationResponseDto> page = listMyInAppNotificationsUseCase
                .list(userId, unreadOnly, pageable)
                .map(this::toDto);
        return ResponseEntity.ok(page);
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark one in-app notification as read")
    public ResponseEntity<Void> markRead(@PathVariable("id") UUID id) {
        UUID userId = extractUserId(SecurityContextHolder.getContext().getAuthentication());
        boolean ok = markInAppNotificationReadUseCase.markRead(id, userId);
        if (!ok) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark all my in-app notifications as read")
    public ResponseEntity<Void> markAllRead() {
        UUID userId = extractUserId(SecurityContextHolder.getContext().getAuthentication());
        markAllInAppNotificationsReadUseCase.markAllRead(userId);
        return ResponseEntity.noContent().build();
    }

    private InAppNotificationResponseDto toDto(InAppNotification notification) {
        return new InAppNotificationResponseDto(
                notification.id(),
                notification.eventType(),
                notification.title(),
                notification.body(),
                notification.deepLink(),
                notification.programId(),
                notification.processId(),
                notification.readAt(),
                notification.createdAt());
    }

    private static UUID extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Authenticated user required");
        }
        if (auth.getPrincipal() instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(auth.getPrincipal().toString());
    }
}
