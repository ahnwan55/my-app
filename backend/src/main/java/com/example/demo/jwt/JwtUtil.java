package com.example.demo.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessExpiration;   // ms 단위 (예: 3600000 = 1시간)
    private final long refreshExpiration;  // ms 단위 (예: 604800000 = 7일)

    // application.yml의 jwt.secret / jwt.access-expiration / jwt.refresh-expiration 주입
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        // HMAC-SHA256 키 생성. secret은 32자 이상 권장
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    // Access Token 생성. subject = kakaoId (문자열)
    public String generateAccessToken(String subject) {
        return buildToken(subject, accessExpiration);
    }

    // Refresh Token 생성
    public String generateRefreshToken(String subject) {
        return buildToken(subject, refreshExpiration);
    }

    private String buildToken(String subject, long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(secretKey)
                .compact();
    }

    // 토큰에서 subject(kakaoId) 추출
    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    // 토큰 유효성 검증. 만료·서명 오류 시 false 반환
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰 만료 여부 확인
    public boolean isExpired(String token) {
        try {
            return parseClaims(token).getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
