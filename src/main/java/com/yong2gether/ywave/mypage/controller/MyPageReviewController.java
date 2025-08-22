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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class MyPageReviewController {

    private final ReviewQueryService reviewQueryService;

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
    @PreAuthorize("@authz.isSelfOrAdmin(#userId, authentication)")
    public ResponseEntity<UserReviewsResponse> getMyReviews(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId,
            Authentication authentication) {
        System.out.println("[DEBUG] reviews principal=" + (authentication != null ? authentication.getName() : null) + ", userId=" + userId);
        List<ReviewItemDto> items = reviewQueryService.getUserReviews(userId);
        return ResponseEntity.ok(
                new UserReviewsResponse("사용자가 작성한 리뷰 목록 조회에 성공했습니다.", items)
        );
    }
}
