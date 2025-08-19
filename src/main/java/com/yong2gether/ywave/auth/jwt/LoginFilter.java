// src/main/java/com/yong2gether/ywave/auth/jwt/LoginFilter.java
package com.yong2gether.ywave.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yong2gether.ywave.auth.dto.ErrorResponse;
import com.yong2gether.ywave.auth.dto.LoginRequest;
import com.yong2gether.ywave.auth.dto.LoginResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/v1/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            LoginRequest login = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword());
            setDetails(request, authToken);
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 파싱 실패", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        String email = authResult.getName();
        String token = jwtUtil.createAccessToken(email);
        long expiresIn = jwtUtil.getDefaultTtlMillis();

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.setHeader("Authorization", "Bearer " + token);

        LoginResponse body = LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInMillis(expiresIn)
                .user(LoginResponse.UserInfo.builder().email(email).build())
                .build();

        objectMapper.writeValue(response.getWriter(), body);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              org.springframework.security.core.AuthenticationException failed)
            throws IOException, ServletException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse err = new ErrorResponse("unauthorized", failed.getMessage());
        objectMapper.writeValue(response.getWriter(), err);
    }
}