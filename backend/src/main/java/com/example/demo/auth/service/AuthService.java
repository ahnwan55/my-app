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

    @Override
    public UserDetails loadUserByUsername(String subject) throws UsernameNotFoundException {
        User user;

        // subject가 숫자면 kakaoId로 조회, 아니면 email로 조회
        if (subject.matches("\\d+")) {
            user = userRepository.findByKakaoId(Long.parseLong(subject))
                    .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다: " + subject));
        } else {
            user = userRepository.findByEmail(subject)
                    .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다: " + subject));
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword() != null ? user.getPassword() : "")
                .roles("USER")
                .build();
    }
}