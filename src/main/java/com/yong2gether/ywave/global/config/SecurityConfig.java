package com.yong2gether.ywave.global.config;

import com.yong2gether.ywave.auth.jwt.JwtFilter;
import com.yong2gether.ywave.auth.jwt.JwtUtil;
import com.yong2gether.ywave.auth.jwt.LoginFilter;
import com.yong2gether.ywave.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 공개 엔드포인트
        List<String> permitAllPaths = List.of(
                "/api/v1/signup",
                "/api/v1/login",
                "/api/v1/duplicate/email",
                "/swagger-ui/**",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/"
        );

        http
                // Stateless + 기본 폼/베이직 인증 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 예외 처리 (커스텀 EntryPoint/DeniedHandler를 나중에 등록해도 됨)
                // .exceptionHandling(e -> e
                //     .authenticationEntryPoint(customEntryPoint)
                //     .accessDeniedHandler(customAccessDeniedHandler)
                // )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(permitAllPaths.toArray(new String[0])).permitAll()
                        // 예: 공공데이터 기반 가맹점 목록/상세는 공개로 둘 수 있음(원하면)
                        // .requestMatchers(HttpMethod.GET, "/api/v1/stores/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 필터 순서: LoginFilter(로그인 시 JWT 발급) -> JwtFilter(요청 검증)
                .addFilterBefore(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtFilter(jwtUtil, userRepository, permitAllPaths),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Origin은 뒤에 슬래시(/) 없이, 필요한 도메인만 명시
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:8080",
                "https://ywave.site",
                "http://localhost:5173",
                "https://ywave-beta.vercel.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        // 클라이언트가 읽을 헤더
        config.setExposedHeaders(List.of("Authorization"));
        // preflight 캐시(선택)
        // config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setHideUserNotFoundExceptions(false); // 사용자 없음 메세지
        return daoAuthenticationProvider;
    }
}
