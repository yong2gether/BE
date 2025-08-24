package com.yong2gether.ywave.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

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

    /* =========================
       토큰 발급
       ========================= */

    /** subject만 넣어 발급 (기존 동작 유지) */
    public String createAccessToken(String subject) {
        return createAccessToken(subject, defaultTtlMillis);
    }

    /** subject만 넣어 발급 + 만료 지정 (기존 동작 유지) */
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

    /** (신규) 커스텀 클레임을 함께 넣어 발급하고 싶을 때 사용 */
    public String createAccessToken(String subject, Map<String, Object> claims, long ttlMillis) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(ttlMillis);
        JwtBuilder builder = Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key, Jwts.SIG.HS256);
        if (claims != null && !claims.isEmpty()) {
            builder.claims(claims);
        }
        return builder.compact();
    }

    /** @Deprecated: 유지 */
    @Deprecated
    public String createAccessToken(String subject, java.util.List<String> roles) {
        return createAccessToken(subject);
    }

    /* =========================
       검증 / 조회
       ========================= */

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

    /** (신규) 안전한 문자열 클레임 조회: 없거나 파싱 실패면 null 반환 */
    public String getClaimAsString(String token, String claimName) {
        try {
            Claims c = parseAllClaims(token);
            Object v = (c != null) ? c.get(claimName) : null;
            return (v == null) ? null : String.valueOf(v);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /** (신규) 제네릭 타입으로 클레임 조회: 타입 불일치/없음이면 null */
    @SuppressWarnings("unchecked")
    public <T> T getClaim(String token, String claimName, Class<T> type) {
        try {
            Claims c = parseAllClaims(token);
            Object v = (c != null) ? c.get(claimName) : null;
            if (v == null) return null;
            if (type.isInstance(v)) return type.cast(v);

            // 간단 캐스팅 보조
            if (type == String.class) return (T) String.valueOf(v);
            if (v instanceof Number n) {
                if (type == Long.class)    return (T) Long.valueOf(n.longValue());
                if (type == Integer.class) return (T) Integer.valueOf(n.intValue());
                if (type == Double.class)  return (T) Double.valueOf(n.doubleValue());
            }
            return null;
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /* =========================
       내부 유틸
       ========================= */

    private Claims parseAllClaims(String token) {
        // JJWT 0.12.x 스타일 파서
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
