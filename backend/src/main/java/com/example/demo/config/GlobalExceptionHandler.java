package com.example.demo.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 공통 에러 응답 형식
    // 모든 에러가 동일한 JSON 구조로 반환되어 프론트엔드에서 일관되게 처리 가능
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    // 400 - 비즈니스 로직 예외 (이메일 중복, 비밀번호 불일치 등)
    // AuthService에서 throw new IllegalArgumentException("...") 할 때 여기서 잡힘
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 400 - @Valid 유효성 검사 실패 (이메일 형식, 비밀번호 길이 등)
    // RequestBody에 @Valid 붙였을 때 검증 실패하면 여기서 잡힘
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        // 첫 번째 필드 에러 메시지만 반환
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("입력값이 올바르지 않습니다.");
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    // 401 - 인증 실패 (JWT 토큰 없거나 만료)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException e) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
    }

    // 403 - 권한 없음 (인증은 됐지만 접근 권한 없음)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException e) {
        return buildResponse(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }

    // 500 - 그 외 예상치 못한 서버 에러
    // 에러 메시지를 그대로 노출하면 보안상 위험하므로 고정 메시지 반환
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
    }
}