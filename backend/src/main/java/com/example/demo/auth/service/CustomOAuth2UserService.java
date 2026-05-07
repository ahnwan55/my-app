package com.example.demo.auth.service;

import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 카카오 응답에서 사용자 정보 추출
        Long kakaoId = (Long) attributes.get("id");

        Map<String, Object> kakaoAccount =
                (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile =
                (Map<String, Object>) kakaoAccount.get("profile");

        String nickname = (String) profile.get("nickname");

        // profile_image_url은 선택 동의 항목이므로 없을 수 있음
        String profileImage = null;
        if (profile.containsKey("profile_image_url")) {
            profileImage = (String) profile.get("profile_image_url");
        }

        // Refresh Token 미리 생성 (DB 저장용)
        String refreshToken = jwtUtil.generateRefreshToken(String.valueOf(kakaoId));

        String finalProfileImage = profileImage;
        String finalRefreshToken = refreshToken;

        // 신규 회원이면 저장, 기존 회원이면 프로필 + refresh_token 갱신
        userRepository.findByKakaoId(kakaoId)
                .map(existingUser -> {
                    existingUser.updateProfile(nickname, finalProfileImage);
                    existingUser.updateRefreshToken(finalRefreshToken);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .kakaoId(kakaoId)
                            .nickname(nickname)
                            .profileImage(finalProfileImage)
                            .build();
                    newUser.updateRefreshToken(finalRefreshToken);
                    return userRepository.save(newUser);
                });

        return oAuth2User;
    }
}
