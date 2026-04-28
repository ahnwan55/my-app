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
            // JWT + 쿠키 기반이므로 CSRF 비활성화
            .csrf(csrf -> csrf.disable())

            // JWT 사용으로 세션 불필요
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 요청별 인가 규칙
            .authorizeHttpRequests(auth -> auth
                    // 로그인, 공개 API는 인증 없이 허용
                    .requestMatchers(
                            "/actuator/**",
                            "/api/auth/**",
                            "/login/**",
                            "/oauth2/**"
                    ).permitAll()
                // 나머지는 인증 필요
                .anyRequest().authenticated()
            )

            // OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                // 카카오 유저 정보 처리 서비스 등록
                .userInfoEndpoint(userInfo ->
                    userInfo.userService(customOAuth2UserService))
                // 로그인 성공 시 JWT 발급 핸들러 등록
                .successHandler(oAuth2SuccessHandler)
            )

            // JWT 검증 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
            .addFilterBefore(
                new JwtFilter(jwtUtil, authService),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
