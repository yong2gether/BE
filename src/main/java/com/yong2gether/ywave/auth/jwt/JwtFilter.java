package com.yong2gether.ywave.auth.jwt;

import com.yong2gether.ywave.auth.userdetails.CustomUserDetails;
import com.yong2gether.ywave.user.domain.User;
import com.yong2gether.ywave.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
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
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String token = header.substring(7);

            if (jwtUtil.validate(token)) {
                // 1) 이메일 우선: email 클레임 → 없으면 sub가 이메일 형식이면 사용
                String emailClaim = safeGetClaim(token, "email");
                String sub = jwtUtil.getSubject(token);

                Optional<User> userOpt = Optional.empty();

                if (emailClaim != null && emailClaim.contains("@")) {
                    userOpt = userRepository.findByEmail(emailClaim);
                }
                if (userOpt.isEmpty() && sub != null && sub.contains("@")) {
                    userOpt = userRepository.findByEmail(sub);
                }

                // 2) 그래도 못 찾으면: sub가 숫자면 userId로 간주하여 조회 시도
                if (userOpt.isEmpty() && sub != null) {
                    try {
                        long userId = Long.parseLong(sub);
                        userOpt = userRepository.findById(userId);
                    } catch (NumberFormatException ignore) {
                        // sub가 숫자 id가 아님
                    }
                }

                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    // 모든 사용자 동등 권한
                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

                    CustomUserDetails principal = new CustomUserDetails(user);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // 토큰은 유효하지만 우리 DB에 유저가 없는 경우
                    // 필요하면 "자동 가입" 로직을 여기에 붙일 수 있음
                    log.warn("JWT valid but user not found. sub={}, emailClaim={}", sub, emailClaim);
                }
            }
        }

        chain.doFilter(request, response);
    }

    private String safeGetClaim(String token, String claimName) {
        try {
            return jwtUtil.getClaimAsString(token, claimName); // 없으면 null 반환하도록 구현
        } catch (Exception e) {
            return null;
        }
    }
}
