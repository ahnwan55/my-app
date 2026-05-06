package com.example.demo.domain.user.service;

import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.domain.library.repository.LibraryRepository;
import com.example.demo.domain.persona.entity.PersonaType;
import com.example.demo.domain.survey.entity.PersonaAnalysis;
import com.example.demo.domain.survey.repository.PersonaAnalysisRepository;
import com.example.demo.domain.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PersonaAnalysisRepository personaAnalysisRepository;
    private final LibraryRepository libraryRepository;

    @Transactional
    public void saveUserInfo(Long userId, UserDto.UserInfoRequest request) {
        findUserOrThrow(userId).updateUserInfo(
                request.getNickname(), request.getGender(), request.getAgeGroup());
    }

    @Transactional(readOnly = true)
    public UserDto.MeResponse getMe(Long userId) {
        User user = findUserOrThrow(userId);

        // 저장된 도서관 코드로 이름을 조회한다. 코드가 없거나 DB에 없으면 null 반환.
        String mainName = user.getMainLibraryCode() != null
                ? libraryRepository.findById(user.getMainLibraryCode())
                        .map(lib -> lib.getName())
                        .orElse(user.getMainLibraryCode())  // 이름 없으면 코드 그대로
                : null;

        String subName = user.getSubLibraryCode() != null
                ? libraryRepository.findById(user.getSubLibraryCode())
                        .map(lib -> lib.getName())
                        .orElse(user.getSubLibraryCode())
                : null;

        return UserDto.MeResponse.of(user, mainName, subName);
    }

    @Transactional(readOnly = true)
    public UserDto.PersonaResponse getPersona(Long userId) {
        User user = findUserOrThrow(userId);
        PersonaType pt = user.getPersonaType();
        if (pt == null) throw new IllegalStateException("페르소나 검사를 먼저 완료해주세요.");
        return UserDto.PersonaResponse.of(pt);
    }

    @Transactional(readOnly = true)
    public List<UserDto.AnalysisHistoryItem> getAnalysisHistory(Long userId) {
        return personaAnalysisRepository
                .findByUser_UserIdOrderByAnalyzedAtDesc(userId)
                .stream()
                .map(UserDto.AnalysisHistoryItem::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateLibraries(Long userId, UserDto.LibraryRequest request) {
        if (request.getMainLibraryCode() == null || request.getMainLibraryCode().isBlank()) {
            throw new IllegalArgumentException("메인 도서관 코드는 필수입니다.");
        }
        findUserOrThrow(userId).updateLibraries(
                request.getMainLibraryCode(),
                request.getSubLibraryCode()
        );
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}
