package com.yong2gether.ywave.user.service;

import com.yong2gether.ywave.user.domain.User;
import com.yong2gether.ywave.user.dto.SignUpRequest;
import com.yong2gether.ywave.user.dto.SignUpResponse;
import com.yong2gether.ywave.user.dto.UserProfileResponse;
import com.yong2gether.ywave.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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

    @Transactional
    public ProfileUpdateResult updateProfile(Long userId, String newNickname, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        
        boolean nicknameChanged = false;
        boolean passwordChanged = false;
        
        // 닉네임이 제공된 경우에만 업데이트
        if (newNickname != null && !newNickname.trim().isEmpty()) {
            // 닉네임 중복 검사 (409, 자신 제외)
            if (!newNickname.equals(user.getNickname()) && 
                userRepository.existsByNickname(newNickname)) {
                throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
            }
            // 닉네임이 실제로 변경되는 경우에만
            if (!newNickname.equals(user.getNickname())) {
                user.changeNickname(newNickname);
                nicknameChanged = true;
            }
        }
        
        // 비밀번호가 제공된 경우에만 업데이트
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            // 비밀번호가 실제로 변경되는 경우에만 (현재 비밀번호와 다를 때)
            // passwordEncoder.matches()를 사용해서 평문 비밀번호 비교
            if (!passwordEncoder.matches(newPassword, user.getPassword())) {
                user.changePassword(passwordEncoder.encode(newPassword));
                passwordChanged = true;
            }
        }
        
        userRepository.save(user);
        
        return new ProfileUpdateResult(nicknameChanged, passwordChanged);
    }

    // 프로필 업데이트 결과를 담는 내부 클래스
    public static class ProfileUpdateResult {
        private final boolean nicknameChanged;
        private final boolean passwordChanged;
        
        public ProfileUpdateResult(boolean nicknameChanged, boolean passwordChanged) {
            this.nicknameChanged = nicknameChanged;
            this.passwordChanged = passwordChanged;
        }
        
        public boolean isNicknameChanged() { return nicknameChanged; }
        public boolean isPasswordChanged() { return passwordChanged; }
    }

    // 클라이언트 에러(409 에러)
    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String message) { super(message); }
    }

    // 닉네임 중복 예외 (409 에러)
    public static class DuplicateNicknameException extends RuntimeException {
        public DuplicateNicknameException(String message) { super(message); }
    }

    // 프로필 조회 서비스
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getPhotoUrl()
        );
    }
}
