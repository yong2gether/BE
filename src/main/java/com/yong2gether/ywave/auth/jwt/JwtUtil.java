package com.yong2gether.ywave.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long defaultTtlMillis;

    public JwtUtil(
            @Value("${jwt.secret}") String base64Secret,
            @Value("${jwt.ttl-millis:3600000}") long defaultTtlMillis
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.defaultTtlMillis = defaultTtlMillis;
    }

    public long getDefaultTtlMillis() { return defaultTtlMillis; }

    // 새로 추가: roles 없이 subject만 담는 토큰
    public String createAccessToken(String subject) {
        return createAccessToken(subject, defaultTtlMillis);
    }

    public String createAccessToken(String subject, long ttlMillis) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(ttlMillis);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    @Deprecated
    public String createAccessToken(String subject, java.util.List<String> roles) {
        return createAccessToken(subject);
    }

    public boolean validate(String token) {
        try {
            parseAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getSubject(String token) {
        return parseAllClaims(token).getSubject();
    }

    private Claims parseAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(stripBearer(token))
                .getPayload();
    }

    private String stripBearer(String token) {
        if (token == null) return "";
        String prefix = "Bearer ";
        return token.startsWith(prefix) ? token.substring(prefix.length()) : token;
    }
}