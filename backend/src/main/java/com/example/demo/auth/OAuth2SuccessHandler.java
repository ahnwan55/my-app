package com.example.demo.auth;

import com.example.demo.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Long kakaoId = (Long) oAuth2User.getAttributes().get("id");

        String accessToken = jwtUtil.generateAccessToken(String.valueOf(kakaoId));
        String refreshToken = jwtUtil.generateRefreshToken(String.valueOf(kakaoId));

        // Access Token 쿠키 설정
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false)        // 로컬 개발 시 false, 배포 시 true로 변경
                .sameSite("Lax")      // 카카오 리다이렉트 허용을 위해 Lax 사용
                .maxAge(Duration.ofMinutes(30))
                .path("/")
                .build();

        // Refresh Token 쿠키 설정
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)        // 로컬 개발 시 false, 배포 시 true로 변경
                .sameSite("Lax")
                .maxAge(Duration.ofDays(7))
                .path("/")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 토큰 없이 메인 페이지로 리다이렉트
        response.sendRedirect("http://localhost:3000/");
    }
}