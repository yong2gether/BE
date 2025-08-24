package com.yong2gether.ywave.mypage.controller;

import com.yong2gether.ywave.mypage.dto.UpdateProfileRequest;
import com.yong2gether.ywave.mypage.dto.UpdateProfileResponse;
import com.yong2gether.ywave.user.service.UserService;
import com.yong2gether.ywave.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "프로필 관리", description = "사용자 프로필 변경 API")
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;

    @Operation(summary = "프로필 변경",
            description = "내부 인증된 사용자 기준으로 프로필을 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자 없음"),
            @ApiResponse(responseCode = "409", description = "닉네임 중복")
    })
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UpdateProfileResponse> updateProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request
    ) {
        String email = (authentication != null ? authentication.getName() : null);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 필요");
        }
        
        Long userId = userRepository.findByEmail(email)
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다."));

        // 닉네임 중복 검사를 컨트롤러에서 직접 수행
        if (request.nickname() != null && !request.nickname().trim().isEmpty()) {
            // 현재 사용자의 닉네임과 다른 경우에만 중복 검사
            String currentNickname = userRepository.findById(userId)
                    .map(u -> u.getNickname())
                    .orElse("");
            
            if (!request.nickname().equals(currentNickname) && 
                userRepository.existsByNickname(request.nickname())) {
                // ResponseEntity.status(409)를 직접 사용
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new UpdateProfileResponse("이미 사용 중인 닉네임입니다.", null));
            }
        }

        try {
            UserService.ProfileUpdateResult result = userService.updateProfile(userId, request.nickname(), request.password());
            return ResponseEntity.ok(UpdateProfileResponse.success(request.nickname(), request.password(), result));
        } catch (Exception e) {
            // 기타 예외는 500으로 처리
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "프로필 변경 중 오류가 발생했습니다.");
        }
    }
}
