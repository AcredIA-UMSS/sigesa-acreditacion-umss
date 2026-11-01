package com.umss.sigesa.adapter.out.auth;

import com.umss.sigesa.domain.model.ExpiredSession;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenAdapterExpiredSessionTest {

    private static final String SECRET = "sigesa-test-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256";

    @Test
    void expiredSignedTokenExposesTheSubjectWithoutTheToken() {
        UUID userId = UUID.randomUUID();
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("email", "jd@umss.edu.bo")
                .expiration(Date.from(Instant.now().minusSeconds(90)))
                .signWith(key)
                .compact();

        ExpiredSession expired = new JwtTokenAdapter(SECRET, 3600).readExpiredSession(token).orElseThrow();

        assertEquals(userId, expired.userId());
        assertEquals("jd@umss.edu.bo", expired.email());
        assertTrue(expired.toString().contains("jd@umss.edu.bo"));
    }

    @Test
    void invalidSignatureIsNotAnExpiredSession() {
        assertTrue(new JwtTokenAdapter(SECRET, 3600).readExpiredSession("not-a-jwt").isEmpty());
    }
}
