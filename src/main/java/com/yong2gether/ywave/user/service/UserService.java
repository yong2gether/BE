package com.yong2gether.ywave.user.service;

import com.yong2gether.ywave.user.domain.User;
import com.yong2gether.ywave.user.dto.SignUpRequest;
import com.yong2gether.ywave.user.dto.SignUpResponse;
import com.yong2gether.ywave.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .photoUrl(request.getPhotoUrl())
                .gpsAllowed(request.getGpsAllowed())
                .build();

        User savedUser = userRepository.save(user);

        return SignUpResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .photoUrl(savedUser.getPhotoUrl())
                .gpsAllowed(savedUser.getGpsAllowed())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
    }

    // 클라이언트 에러(409 에러)
    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String message) { super(message); }
    }
}
