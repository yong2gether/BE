// src/main/java/com/yong2gether/ywave/mypage/controller/MyPageReviewController.java
// 토큰 이메일 -> User.Id 조회 -> path의 {userId}와 일치할때만 가능
// 실제 데이터는 ReviewQueryService 호출
package com.yong2gether.ywave.mypage.controller;

import com.yong2gether.ywave.mypage.dto.UserReviewsResponse;
import com.yong2gether.ywave.mypage.dto.ReviewItemDto;
import com.yong2gether.ywave.mypage.service.ReviewQueryService;
import com.yong2gether.ywave.user.domain.User;
import com.yong2gether.ywave.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class MyPageReviewController {

    private final ReviewQueryService reviewQueryService;
    private final UserRepository userRepository;

    @Operation(
            summary = "내가 쓴 리뷰 조회",
            description = "userId 기준으로 해당 사용자가 작성한 모든 리뷰를 조회(디폴트:최신순)합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserReviewsResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/{userId}/reviews") // API URL : GET /api/v1/mypage/{userId}/reviews
    public ResponseEntity<UserReviewsResponse> getMyReviews(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId) {

        // ★ 토큰(이메일)로 본인 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = auth.getName(); // JwtFilter에서 principal로 넣은 값(이메일)

        User me = userRepository.findByEmail(email)
                .orElse(null);
        if (me == null || !me.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ReviewItemDto> items = reviewQueryService.getUserReviews(userId);
        return ResponseEntity.ok(
                new UserReviewsResponse("사용자가 작성한 리뷰 목록 조회에 성공했습니다.", items)
        );
    }
}
