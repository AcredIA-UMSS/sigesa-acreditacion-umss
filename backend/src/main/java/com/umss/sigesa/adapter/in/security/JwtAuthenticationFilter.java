package com.umss.sigesa.adapter.in.security;

import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.domain.model.ExpiredSession;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenAdapter jwtTokenAdapter;
    private final AuditLogPort auditLogPort;

    public JwtAuthenticationFilter(JwtTokenAdapter jwtTokenAdapter, AuditLogPort auditLogPort) {
        this.jwtTokenAdapter = jwtTokenAdapter;
        this.auditLogPort = auditLogPort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtTokenAdapter.isValid(token)) {
                UUID userId = jwtTokenAdapter.parseUserId(token);
                String role = jwtTokenAdapter.parseRole(token);
                List<UUID> programScope = jwtTokenAdapter.parseProgramScope(token);
                var authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                // Detalle: alcance de carrera del JWT (sobrevive reinicios H2 si el claim es estable).
                authentication.setDetails(programScope);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                recordExpiredSession(token);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void recordExpiredSession(String token) {
        try {
            java.util.Optional<ExpiredSession> expired = jwtTokenAdapter.readExpiredSession(token);
            if (expired != null && expired.isPresent()) {
                ExpiredSession session = expired.get();
                auditLogPort.logSessionExpired(session.userId(), session.email());
            }
        } catch (RuntimeException ignored) {
            // La petición sigue sin autenticación si la bitácora no puede escribir.
        }
    }
}
