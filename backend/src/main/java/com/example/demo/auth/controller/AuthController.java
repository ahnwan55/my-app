package com.example.demo.auth.controller;

import com.example.demo.auth.dto.LoginRequest;
import com.example.demo.auth.dto.SignupRequest;
import com.example.demo.auth.dto.TokenResponse;
import com.example.demo.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest req) {
        authService.signup(req);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        TokenResponse token = authService.login(req);
        return ResponseEntity.ok(token);
    }
}