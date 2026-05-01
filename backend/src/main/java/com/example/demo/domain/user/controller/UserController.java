package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.dto.UserDto;
import com.example.demo.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UserController — 마이페이지 관련 API 컨트롤러
 *
 * [엔드포인트]
 *   POST  /api/users/info              — 닉네임/성별/연령대 저장 (UserInfoPage)
 *   GET   /api/users/me                — 내 프로필 조회
 *   GET   /api/users/me/persona        — 현재 페르소나 조회
 *   GET   /api/users/me/analysis-history — 분석 이력 조회
 *   PATCH /api/users/me/libraries      — 등록 도서관 저장
 *
 * [인증]
 *   모든 엔드포인트는 로그인 필요 (JWT 쿠키 검증)
 *   @AuthenticationPrincipal로 현재 로그인 사용자 ID를 주입받는다.
 *   SecurityConfig에서 /api/users/** 경로를 authenticated()로 설정해야 한다.
 */
@Tag(name = "User", description = "마이페이지 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 최초 로그인 후 사용자 정보 저장
     * UserInfoPage에서 닉네임/성별/연령대 입력 후 호출된다.
     *
     * POST /api/users/info
     * Body: { "nickname": "시완", "gender": "male", "ageGroup": "20s" }
     */
    @Operation(summary = "사용자 정보 저장", description = "최초 로그인 후 닉네임/성별/연령대를 저장한다.")
    @PostMapping("/info")
    public ResponseEntity<Void> saveUserInfo(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserDto.UserInfoRequest request
    ) {
        userService.saveUserInfo(userId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 내 프로필 조회
     * MyPage 프로필 카드 + 등록 도서관 정보를 반환한다.
     *
     * GET /api/users/me
     */
    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<UserDto.MeResponse> getMe(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(userService.getMe(userId));
    }

    /**
     * 현재 페르소나 조회
     * MyPage 현재 페르소나 카드에 사용된다.
     * 페르소나 검사 미완료 시 400 반환.
     *
     * GET /api/users/me/persona
     */
    @Operation(summary = "현재 페르소나 조회")
    @GetMapping("/me/persona")
    public ResponseEntity<UserDto.PersonaResponse> getPersona(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(userService.getPersona(userId));
    }

    /**
     * 분석 이력 조회
     * MyPage 타임라인에 사용된다. 최신순 정렬.
     *
     * GET /api/users/me/analysis-history
     */
    @Operation(summary = "페르소나 분석 이력 조회")
    @GetMapping("/me/analysis-history")
    public ResponseEntity<List<UserDto.AnalysisHistoryItem>> getAnalysisHistory(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(userService.getAnalysisHistory(userId));
    }

    /**
     * 마이페이지 도서관 등록
     * mainLibraryCode는 필수, subLibraryCode1/2는 선택.
     * 저장된 값은 InventoryService가 재고 자동 조회에 활용한다.
     *
     * PATCH /api/users/me/libraries
     * Body: {
     *   "mainLibraryCode": "111001",
     *   "subLibraryCode1": "111002",
     *   "subLibraryCode2": null
     * }
     */
    @Operation(summary = "등록 도서관 저장")
    @PatchMapping("/me/libraries")
    public ResponseEntity<Void> updateLibraries(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserDto.LibraryRequest request
    ) {
        userService.updateLibraries(userId, request);
        return ResponseEntity.ok().build();
    }
}
