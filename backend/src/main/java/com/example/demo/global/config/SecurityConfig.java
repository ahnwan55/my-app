package com.example.demo.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * SecurityConfig — Spring Security 설정
 *
 * [현재 상태] JWT 인증 구현 전 개발용 설정
 *   - 모든 API 요청을 허용합니다 (인증 없이 호출 가능)
 *   - CORS는 로컬 개발 환경(localhost:3000)을 허용합니다
 *
 * [JWT 추가 시 변경할 부분]
 *   1. JwtAuthenticationFilter 클래스 작성
 *   2. 아래 TODO 주석 위치에 필터 추가
 *   3. authorizeHttpRequests()에서 보호할 경로 지정
 *
 * @Configuration: 이 클래스가 Spring 설정 클래스임을 나타냅니다.
 * @EnableWebSecurity: Spring Security를 활성화합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * HTTP 보안 설정의 핵심 Bean입니다.
     *
     * SecurityFilterChain: HTTP 요청이 들어올 때 적용할 보안 필터 체인입니다.
     * Spring Boot 3.x부터는 WebSecurityConfigurerAdapter 상속 대신
     * 이 방식(Bean으로 등록)을 사용합니다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ── CSRF 비활성화 ──────────────────────────────────────────
                // CSRF(Cross-Site Request Forgery) 공격 방어 기능입니다.
                // REST API + JWT 방식에서는 세션을 사용하지 않으므로 불필요합니다.
                // 세션 기반 인증(form login)을 쓸 경우에는 활성화해야 합니다.
                .csrf(AbstractHttpConfigurer::disable)

                // ── CORS 설정 적용 ─────────────────────────────────────────
                // 아래 corsConfigurationSource() Bean을 사용합니다.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ── 세션 정책: STATELESS ───────────────────────────────────
                // JWT를 사용하면 서버에 세션을 저장하지 않습니다.
                // STATELESS로 설정하면 SecurityContext를 세션에 저장하지 않습니다.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ── 요청별 인가(Authorization) 규칙 ───────────────────────
                .authorizeHttpRequests(auth -> auth
                        // [개발용] 현재는 모든 요청 허용
                        // TODO: JWT 구현 후 아래처럼 변경
                        // .requestMatchers("/api/surveys/**").permitAll()   // 설문은 인증 불필요
                        // .requestMatchers("/api/products/**").permitAll()  // 상품 조회는 인증 불필요
                        // .requestMatchers("/api/recommendations/**").authenticated() // 추천은 인증 필요
                        // .anyRequest().authenticated()
                        .anyRequest().permitAll()
                )

                // ── 기본 폼 로그인 비활성화 ────────────────────────────────
                // Spring Security 기본 로그인 페이지를 비활성화합니다.
                // REST API 서버이므로 필요 없습니다.
                .formLogin(AbstractHttpConfigurer::disable)

                // ── HTTP Basic 인증 비활성화 ───────────────────────────────
                // Authorization: Basic base64(id:pw) 방식을 비활성화합니다.
                .httpBasic(AbstractHttpConfigurer::disable);

        // TODO: JWT 필터 추가 (JWT 구현 후 아래 주석 해제)
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정
     *
     * CORS: 브라우저가 다른 출처(Origin)의 API를 호출할 때 적용되는 보안 정책입니다.
     * React(localhost:3000) → Spring Boot(localhost:8080) 호출 시 CORS 오류가 발생하는데,
     * 여기서 허용 출처를 설정해주면 해결됩니다.
     *
     * 운영 환경에서는 allowedOrigins를 실제 프론트엔드 도메인으로 교체해야 합니다.
     * 예: "https://youngfinance.example.com"
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 출처 (Origin)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",    // React 로컬 개발 서버
                "http://localhost:5173"     // Vite 사용 시
                // TODO: 운영 배포 후 CloudFront 도메인 추가
                // "https://d1234abcd.cloudfront.net"
        ));

        // 허용할 HTTP 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 허용할 요청 헤더
        config.setAllowedHeaders(List.of("*"));

        // 인증 정보(쿠키, Authorization 헤더) 포함 허용
        config.setAllowCredentials(true);

        // preflight 요청 캐시 시간 (초) — OPTIONS 요청 횟수를 줄여 성능 향상
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);  // 모든 경로에 적용
        return source;
    }
}