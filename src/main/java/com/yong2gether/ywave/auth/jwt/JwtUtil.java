package com.yong2gether.ywave.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long defaultTtlMillis;

    public JwtUtil(
            @Value("${jwt.secret}") String base64Secret,
            @Value("${jwt.ttl-millis:3600000}") long defaultTtlMillis // 기본 1시간
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.defaultTtlMillis = defaultTtlMillis;
    }

    public String createAccessToken(String subject, List<String> roles) {
        return createAccessToken(subject, roles, defaultTtlMillis);
    }

    public String createAccessToken(String subject, List<String> roles, long ttlMillis) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(ttlMillis);

        return Jwts.builder()
                .subject(subject)                 // 일반적으로 이메일/아이디
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(Map.of("roles", roles))   // 커스텀 클레임
                .signWith(key, Jwts.SIG.HS256)
                .compact();
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

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Object val = parseAllClaims(token).get("roles");
        if (val instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
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