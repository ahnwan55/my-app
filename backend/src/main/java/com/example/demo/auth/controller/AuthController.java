package com.example.demo.auth.controller;

import com.example.demo.auth.dto.TokenResponse;
import com.example.demo.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    // 현재 로그인한 사용자 토큰 재발급
    @GetMapping("/me")
    public ResponseEntity<TokenResponse> me(@AuthenticationPrincipal OAuth2User oAuth2User) {
        Long kakaoId = (Long) oAuth2User.getAttributes().get("id");
        String accessToken = jwtUtil.generateAccessToken(String.valueOf(kakaoId));
        String refreshToken = jwtUtil.generateRefreshToken(String.valueOf(kakaoId));
        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
    }
}