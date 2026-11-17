package com.umss.sigesa.adapter.in.security;

import com.umss.sigesa.adapter.out.auth.JwtTokenAdapter;
import com.umss.sigesa.application.access.AccessSessionHolder;
import com.umss.sigesa.application.port.out.AccessSessionPort;
import com.umss.sigesa.domain.model.access.AccessSessionEndReason;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
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
    private final AccessSessionPort accessSessionPort;

    public JwtAuthenticationFilter(JwtTokenAdapter jwtTokenAdapter, @Lazy AccessSessionPort accessSessionPort) {
        this.jwtTokenAdapter = jwtTokenAdapter;
        this.accessSessionPort = accessSessionPort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = header.substring(7).trim();
            if (!token.isEmpty() && jwtTokenAdapter.isValid(token)) {
                UUID userId = jwtTokenAdapter.parseUserId(token);
                String role = jwtTokenAdapter.parseRole(token);
                List<UUID> programScope = jwtTokenAdapter.parseProgramScope(token);
                UUID sessionId = jwtTokenAdapter.parseSessionId(token);
                var authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                authentication.setDetails(programScope);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String email = jwtTokenAdapter.parseEmail(token);
                if (email != null) {
                    request.setAttribute(AccessSessionHolder.ACTOR_EMAIL_ATTRIBUTE, email);
                }
                if (sessionId != null) {
                    request.setAttribute(AccessSessionHolder.REQUEST_ATTRIBUTE, sessionId);
                    AccessSessionHolder.bind(sessionId);
                }
            } else if (!token.isEmpty()) {
                UUID expiredSession = jwtTokenAdapter.parseSessionIdAllowExpired(token);
                if (expiredSession != null) {
                    accessSessionPort.close(expiredSession, AccessSessionEndReason.EXPIRED);
                }
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            AccessSessionHolder.clear();
        }
    }
}
