package com.yong2gether.ywave.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/v1/login"); // 로그인 엔드포인트
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            LoginRequest login = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword());
            setDetails(request, authToken);
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 파싱 실패", e);
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult
    ) throws IOException, ServletException {

        // 여기서는 principal의 username을 subject로 사용
        String subject = authResult.getName();

        // 권한 문자열 리스트 추출
        List<String> roles = authResult.getAuthorities().stream()
                .map(granted -> granted.getAuthority())
                .toList();

        String token = jwtUtil.createAccessToken(subject, roles);

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.setHeader("Authorization", "Bearer " + token);

        // 만료 시각 계산(프런트 편의를 위해)
        Instant now = Instant.now();
        String body = objectMapper.writeValueAsString(Map.of(
                "accessToken", token,
                "tokenType", "Bearer",
                "expiresInMillis", 3600000,   // JwtUtil 기본값과 맞춰주세요
                "user", Map.of(
                        "username", subject,
                        "roles", roles
                )
        ));
        response.getWriter().write(body);
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException failed
    ) throws IOException, ServletException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String body = objectMapper.writeValueAsString(Map.of(
                "error", "unauthorized",
                "message", failed.getMessage()
        ));
        response.getWriter().write(body);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String username; // 이메일을 username으로 쓸 경우, 필드명은 그대로 username
        private String password;
    }
}