package com.example.demo.config;

import com.example.demo.auth.OAuth2SuccessHandler;
import com.example.demo.auth.service.AuthService;
import com.example.demo.auth.service.CustomOAuth2UserService;
import com.example.demo.jwt.JwtFilter;
import com.example.demo.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * SecurityConfig
 *
 * [변경 사항]
 *   - oauth2Login에 loginPage("/login") 추가
 *     인증 필요한 API 접근 시 OAuth2가 자동으로 카카오 로그인으로
 *     리다이렉트하는 문제 해결. /login 페이지로 보내도록 명시.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 테스트 endpoint 공개
                        .requestMatchers("/chaos-test/**").permitAll()

                        // 기존 공개 endpoint들
                        .requestMatchers(
                                "/actuator/**",
                                "/api/auth/**",
                                "/login/**",
                                "/oauth2/**",
                                "/api/books/**",
                                "/api/libraries/**",
                                "/api/inventory"
                        ).permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")  // 인증 필요 시 /login으로 이동 (OAuth2 자동 리다이렉트 방지)
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )
                .addFilterBefore(
                        new JwtFilter(jwtUtil, authService),
                        UsernamePasswordAuthenticationFilter.class
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "https://bookjjeok.cloud",
                "https://www.bookjjeok.cloud",
                "http://43.200.135.188",
                "http://localhost",
                "http://localhost:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
