package com.yong2gether.ywave.auth.jwt;

import com.yong2gether.ywave.user.domain.User;
import com.yong2gether.ywave.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collection;
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
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header;

            if (jwtUtil.validate(token)) {
                String subject = jwtUtil.getSubject(token); // 일반적으로 이메일
                List<String> roles = jwtUtil.getRoles(token);

                // 이미 인증된 상태가 아니면 SecurityContext에 주입
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    // DB에서 사용자 조회(필요 시 캐시 고려)
                    Optional<User> userOpt = userRepository.findByEmail(subject);

                    // 사용자 존재 확인 후 권한 구성
                    Collection<SimpleGrantedAuthority> authorities =
                            roles.stream().map(SimpleGrantedAuthority::new).toList();

                    // UserDetails를 따로 구현하지 않았다면 username만 넣어도 동작
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userOpt.map(User::getEmail).orElse(subject), // principal
                                    null,                                          // credentials
                                    authorities                                    // authorities
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}