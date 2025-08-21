package com.yong2gether.ywave.auth.jwt;

import com.yong2gether.ywave.user.domain.User;
import com.yong2gether.ywave.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // ★ 추가
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final List<String> permitAllPaths;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository, List<String> permitAllPaths) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.permitAllPaths = permitAllPaths;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return permitAllPaths.stream().anyMatch(p -> matcher.match(p, uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // 유효 토큰 + 아직 인증 안되어 있으면 진행
            if (jwtUtil.validate(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 우리 설계: subject = email
                String email = jwtUtil.getSubject(token);

                // DB 사용자 확인
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {

                    // ★ 권한 부여: 최소한 ROLE_USER 한 개는 넣어준다
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

                    // principal은 email로 유지 (원하면 userOpt.get() 로 바꿔도 됨)
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    null,
                                    authorities // ★ emptyList() → ROLE_USER 부여
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
