package com.example.demo.auth.controller;

import com.example.demo.auth.dto.TokenResponse;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /**
     * Access Token 재발급 (Refresh Token 검증 후 신규 발급)
     * 쿠키의 refreshToken을 읽어 유효하면 새 accessToken 쿠키를 발급한다.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        // refreshToken 쿠키가 없거나 만료된 경우 401 반환
        if (refreshToken == null || !jwtUtil.isValid(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        String kakaoId = jwtUtil.getSubject(refreshToken);

        // DB에 저장된 refresh_token과 비교 (탈취 방지)
        User user = userRepository.findByKakaoId(Long.parseLong(kakaoId))
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        if (!refreshToken.equals(user.getRefreshToken())) {
            return ResponseEntity.status(401).build();
        }

        // 새 Access Token 발급 후 쿠키 갱신
        String newAccessToken = jwtUtil.generateAccessToken(kakaoId);
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(false)        // 배포 시 true로 변경
                .sameSite("Lax")
                .maxAge(Duration.ofMinutes(30))
                .path("/")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        return ResponseEntity.ok().build();
    }

    /**
     * 로그아웃
     * DB의 refresh_token을 null로 초기화하고 쿠키를 만료시킨다.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response) {

        // DB refresh_token 초기화
        userRepository.findByKakaoId(Long.parseLong(userDetails.getUsername()))
                .ifPresent(user -> {
                    user.updateRefreshToken(null);
                    userRepository.save(user);
                });

        // 쿠키 만료 처리 (maxAge = 0)
        expireCookie(response, "accessToken");
        expireCookie(response, "refreshToken");

        return ResponseEntity.ok().build();
    }

    /**
     * 회원 탈퇴
     * DB에서 유저를 삭제하고 쿠키를 만료시킨다.
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response) {

        userRepository.findByKakaoId(Long.parseLong(userDetails.getUsername()))
                .ifPresent(userRepository::delete);

        expireCookie(response, "accessToken");
        expireCookie(response, "refreshToken");

        return ResponseEntity.ok().build();
    }

    // 쿠키 만료 처리 공통 메서드
    private void expireCookie(HttpServletResponse response, String cookieName) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(false)        // 배포 시 true로 변경
                .sameSite("Lax")
                .maxAge(0)            // 즉시 만료
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
