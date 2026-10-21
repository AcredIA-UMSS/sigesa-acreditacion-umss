package com.umss.sigesa.adapter.in.web;

import com.umss.sigesa.adapter.in.security.JwtProgramScopeSupport;
import com.umss.sigesa.adapter.in.web.dto.NotificationResponseDto;
import com.umss.sigesa.adapter.in.web.dto.NotificationUnreadCountDto;
import com.umss.sigesa.application.port.in.GetNotificationsUseCase;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Centro de notificaciones y alertas transaccionales (FSD-UC-015 / FEAT-05)")
public class NotificationController {

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort;

    public NotificationController(
            GetNotificationsUseCase getNotificationsUseCase,
            UserProgramAssignmentRepositoryPort userProgramAssignmentRepositoryPort) {
        this.getNotificationsUseCase = getNotificationsUseCase;
        this.userProgramAssignmentRepositoryPort = userProgramAssignmentRepositoryPort;
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener notificaciones del usuario autenticado",
               description = "Retorna lista paginada de notificaciones para el usuario o su carrera.")
    public ResponseEntity<Page<NotificationResponseDto>> getMyNotifications(
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = extractUserId(auth);
        List<UUID> programScopes = extractProgramScopes(auth, userId);
        String email = auth != null && auth.getName() != null ? auth.getName() : "";

        Page<NotificationResponseDto> result = getNotificationsUseCase
                .getNotifications(userId, programScopes, email, unreadOnly, pageable)
                .map(NotificationResponseDto::from);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Obtener conteo de notificaciones no leídas",
               description = "Retorna el número de alertas pendientes para badges de UI.")
    public ResponseEntity<NotificationUnreadCountDto> getUnreadCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = extractUserId(auth);
        List<UUID> programScopes = extractProgramScopes(auth, userId);
        String email = auth != null && auth.getName() != null ? auth.getName() : "";

        long count = getNotificationsUseCase.getUnreadCount(userId, programScopes, email);
        return ResponseEntity.ok(new NotificationUnreadCountDto(count));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar notificación como leída",
               description = "Actualiza el timestamp readAt para la notificación especificada.")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        getNotificationsUseCase.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/mark-all-read")
    @Operation(summary = "Marcar todas las notificaciones como leídas",
               description = "Actualiza masivamente el estado de no leídas para el usuario autenticado.")
    public ResponseEntity<Void> markAllAsRead() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = extractUserId(auth);
        List<UUID> programScopes = extractProgramScopes(auth, userId);
        String email = auth != null && auth.getName() != null ? auth.getName() : "";

        getNotificationsUseCase.markAllAsRead(userId, programScopes, email);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return UUID.randomUUID();
        }
        if (auth.getPrincipal() instanceof UUID uuid) {
            return uuid;
        }
        try {
            return UUID.fromString(auth.getPrincipal().toString());
        } catch (Exception e) {
            return UUID.randomUUID();
        }
    }

    private List<UUID> extractProgramScopes(Authentication auth, UUID userId) {
        List<UUID> jwtScopes = JwtProgramScopeSupport.programScopeFromAuthentication(auth);
        if (!jwtScopes.isEmpty()) {
            return jwtScopes;
        }
        try {
            List<UserProgramAssignment> assignments = userProgramAssignmentRepositoryPort.findActiveByUserId(userId);
            return assignments.stream().map(UserProgramAssignment::getProgramId).toList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
