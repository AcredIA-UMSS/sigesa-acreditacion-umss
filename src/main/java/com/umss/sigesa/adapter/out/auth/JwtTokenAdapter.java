package com.umss.sigesa.adapter.out.auth;

import com.umss.sigesa.application.port.out.IssuedToken;
import com.umss.sigesa.application.port.out.TokenPort;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenAdapter implements TokenPort {

    private final SecretKey secretKey;
    private final long expirationSeconds;

    public JwtTokenAdapter(
            @Value("${sigesa.jwt.secret}") String secret,
            @Value("${sigesa.jwt.expiration-seconds:86400}") long expirationSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public IssuedToken issue(AuthenticatedIdentity identity) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationSeconds);

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", identity.email().value());
        claims.put("role", identity.role().name());
        claims.put("programScope", identity.programScope().stream().map(UUID::toString).toList());

        String token = Jwts.builder()
                .subject(identity.userId().toString())
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();

        return new IssuedToken(token, expirationSeconds);
    }

    public UUID parseUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public String parseRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<UUID> parseProgramScope(String token) {
        List<String> raw = parseClaims(token).get("programScope", List.class);
        if (raw == null) {
            return List.of();
        }
        return raw.stream().map(UUID::fromString).toList();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private io.jsonwebtoken.Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
