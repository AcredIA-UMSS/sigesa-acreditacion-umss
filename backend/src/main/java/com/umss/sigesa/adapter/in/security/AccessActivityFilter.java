package com.umss.sigesa.adapter.in.security;

import com.umss.sigesa.adapter.out.persistance.AuditLogJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AuditLogJpaEntity;
import com.umss.sigesa.application.access.AccessSessionHolder;
import com.umss.sigesa.config.security.SecretSanitizer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AccessActivityFilter extends OncePerRequestFilter {

    private final AuditLogJpaRepository auditLogRepository;

    public AccessActivityFilter(AuditLogJpaRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(request, response);
        if (!shouldRecord(request)) {
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)) {
            return;
        }
        Object sessionAttribute = request.getAttribute(AccessSessionHolder.REQUEST_ATTRIBUTE);
        UUID sessionId = sessionAttribute instanceof UUID id ? id : null;
        String path = request.getRequestURI();
        String outcome = response.getStatus() >= 400 ? "FALLIDO" : "EXITOSO";
        String description = outcome + ": " + request.getMethod() + " " + path;
        AuditLogJpaEntity entity = AuditLogJpaEntity.builder()
                .id(UUID.randomUUID())
                .actorId(userId)
                .actorEmail(emailOf(request))
                .actorRole(roleOf(authentication))
                .action(actionOf(request.getMethod()))
                .entityType(moduleOf(path))
                .entityId(sessionId != null ? sessionId.toString() : path)
                .payloadDiff(SecretSanitizer.sanitizeValue("payload", description))
                .ipAddress(clientIp(request))
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(entity);
    }

    private static boolean shouldRecord(HttpServletRequest request) {
        if (Boolean.TRUE.equals(request.getAttribute(AccessSessionHolder.AUDITED_ATTRIBUTE))) {
            return false;
        }
        String path = request.getRequestURI();
        if (path == null || !path.startsWith("/api/")) {
            return false;
        }
        if (path.startsWith("/api/v1/access-sessions") || path.startsWith("/api/v1/auth/")) {
            return false;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        return request.getAttribute(AccessSessionHolder.REQUEST_ATTRIBUTE) instanceof UUID;
    }

    private static String actionOf(String method) {
        return switch (method) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> "READ";
        };
    }

    private static String moduleOf(String path) {
        if (path.contains("/admin/users")) return "USUARIOS";
        if (path.contains("/processes")) return "PROCESOS";
        if (path.contains("/evidence") || path.contains("/document-assets") || path.contains("/indicators")) return "EVIDENCIAS";
        if (path.contains("/report")) return "REPORTES";
        if (path.contains("/dictamen") || path.contains("/inbox")) return "DICTAMENES";
        if (path.contains("/evaluation") || path.contains("/stage")) return "EVALUACIONES";
        if (path.contains("/news")) return "NOTICIAS";
        return "SISTEMA";
    }

    private static String roleOf(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .findFirst()
                .orElse("UNKNOWN");
    }

    private static String emailOf(HttpServletRequest request) {
        Object email = request.getAttribute(AccessSessionHolder.ACTOR_EMAIL_ATTRIBUTE);
        return email instanceof String value && !value.isBlank() ? value : "desconocido";
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
