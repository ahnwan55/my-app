package com.example.demo.domain.user.service;

import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.domain.persona.entity.PersonaType;
import com.example.demo.domain.survey.entity.PersonaAnalysis;
import com.example.demo.domain.survey.repository.PersonaAnalysisRepository;
import com.example.demo.domain.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserService — 마이페이지 관련 비즈니스 로직
 *
 * [담당 API]
 *   POST  /api/users/info              — 닉네임/성별/연령대 저장
 *   GET   /api/users/me                — 내 프로필 조회
 *   GET   /api/users/me/persona        — 현재 페르소나 조회
 *   GET   /api/users/me/analysis-history — 분석 이력 조회
 *   PATCH /api/users/me/libraries      — 등록 도서관 저장
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PersonaAnalysisRepository personaAnalysisRepository;

    // ── POST /api/users/info ──────────────────────────────────────

    /**
     * UserInfoPage 제출 정보 저장
     * 최초 로그인 후 1회 호출된다.
     */
    @Transactional
    public void saveUserInfo(Long userId, UserDto.UserInfoRequest request) {
        User user = findUserOrThrow(userId);
        user.updateUserInfo(request.getNickname(), request.getGender(), request.getAgeGroup());
    }

    // ── GET /api/users/me ─────────────────────────────────────────

    /**
     * 내 프로필 조회
     * MyPage 프로필 카드 + 등록 도서관 정보 반환
     */
    @Transactional(readOnly = true)
    public UserDto.MeResponse getMe(Long userId) {
        User user = findUserOrThrow(userId);
        return UserDto.MeResponse.of(user);
    }

    // ── GET /api/users/me/persona ─────────────────────────────────

    /**
     * 현재 페르소나 조회
     * User.personaType이 null이면 404 반환 (페르소나 검사 미완료 상태)
     */
    @Transactional(readOnly = true)
    public UserDto.PersonaResponse getPersona(Long userId) {
        User user = findUserOrThrow(userId);
        PersonaType pt = user.getPersonaType();

        if (pt == null) {
            // 페르소나 검사를 아직 하지 않은 경우
            throw new IllegalStateException("페르소나 검사를 먼저 완료해주세요.");
        }

        return UserDto.PersonaResponse.of(pt);
    }

    // ── GET /api/users/me/analysis-history ───────────────────────

    /**
     * 분석 이력 조회
     * analyzed_at 내림차순 (최신 순)으로 반환한다.
     */
    @Transactional(readOnly = true)
    public List<UserDto.AnalysisHistoryItem> getAnalysisHistory(Long userId) {
        List<PersonaAnalysis> history =
                personaAnalysisRepository.findByUser_UserIdOrderByAnalyzedAtDesc(userId);

        return history.stream()
                .map(UserDto.AnalysisHistoryItem::of)
                .collect(Collectors.toList());
    }

    // ── PATCH /api/users/me/libraries ────────────────────────────

    /**
     * 마이페이지 도서관 등록
     * mainLibraryCode는 필수, subLibraryCode1/2는 선택이다.
     * InventoryService가 이 값을 읽어 재고를 자동 조회한다.
     */
    @Transactional
    public void updateLibraries(Long userId, UserDto.LibraryRequest request) {
        if (request.getMainLibraryCode() == null || request.getMainLibraryCode().isBlank()) {
            throw new IllegalArgumentException("메인 도서관 코드는 필수입니다.");
        }

        User user = findUserOrThrow(userId);
        user.updateLibraries(
                request.getMainLibraryCode(),
                request.getSubLibraryCode1(),
                request.getSubLibraryCode2()
        );
    }

    // ── 내부 유틸 ─────────────────────────────────────────────────

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. userId=" + userId));
    }
}
