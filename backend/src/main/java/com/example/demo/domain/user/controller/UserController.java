package com.example.demo.domain.user.controller;

import com.example.demo.auth.repository.UserRepository;
import com.example.demo.domain.user.dto.UserDto;
import com.example.demo.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UserController — 마이페이지 관련 API 컨트롤러
 *
 * [userId 획득 방식]
 *   JWT subject = kakaoId(String)
 *   → UserDetails.getUsername() = kakaoId
 *   → UserRepository로 kakaoId → userId(Long) 변환
 */
@Tag(name = "User", description = "마이페이지 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    // UserDetails → userId(Long) 변환 공통 메서드
    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        Long kakaoId = Long.parseLong(userDetails.getUsername());
        return userRepository.findByKakaoId(kakaoId)
                .map(user -> user.getUserId())
                .orElse(null);
    }

    @Operation(summary = "사용자 정보 저장")
    @PostMapping("/info")
    public ResponseEntity<Void> saveUserInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserDto.UserInfoRequest request
    ) {
        userService.saveUserInfo(resolveUserId(userDetails), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<UserDto.MeResponse> getMe(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.getMe(resolveUserId(userDetails)));
    }

    @Operation(summary = "현재 페르소나 조회")
    @GetMapping("/me/persona")
    public ResponseEntity<UserDto.PersonaResponse> getPersona(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.getPersona(resolveUserId(userDetails)));
    }

    @Operation(summary = "페르소나 분석 이력 조회")
    @GetMapping("/me/analysis-history")
    public ResponseEntity<List<UserDto.AnalysisHistoryItem>> getAnalysisHistory(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.getAnalysisHistory(resolveUserId(userDetails)));
    }

    @Operation(summary = "등록 도서관 저장")
    @PatchMapping("/me/libraries")
    public ResponseEntity<Void> updateLibraries(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserDto.LibraryRequest request
    ) {
        userService.updateLibraries(resolveUserId(userDetails), request);
        return ResponseEntity.ok().build();
    }
}
