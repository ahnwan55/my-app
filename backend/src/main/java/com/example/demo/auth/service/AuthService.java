package com.example.demo.auth.service;

import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * JwtFilter에서 토큰의 subject(kakaoId)로 사용자를 조회할 때 호출된다.
     * subject가 숫자이면 kakaoId로, 아니면 email로 조회한다.
     */
    @Override
    public UserDetails loadUserByUsername(String subject) throws UsernameNotFoundException {
        User user;

        if (subject.matches("\\d+")) {
            user = userRepository.findByKakaoId(Long.parseLong(subject))
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "유저를 찾을 수 없습니다: " + subject));
        } else {
            user = userRepository.findByKakaoId(Long.parseLong(subject))
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "유저를 찾을 수 없습니다: " + subject));
        }

        // SecurityContext에 등록할 UserDetails 반환
        // password는 OAuth2 방식이므로 빈 문자열로 처리
        return org.springframework.security.core.userdetails.User
                .withUsername(String.valueOf(user.getKakaoId()))
                .password(user.getRefreshToken() != null ? "" : "")
                .roles("USER")
                .build();
    }
}
