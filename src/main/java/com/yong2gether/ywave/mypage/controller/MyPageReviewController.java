// src/main/java/com/yong2gether/ywave/mypage/controller/MyPageReviewController.java
// 토큰 이메일 -> User.Id 조회 -> path의 {userId}와 일치할때만 가능
// 실제 데이터는 ReviewQueryService 호출
package com.yong2gether.ywave.mypage.controller;

import com.yong2gether.ywave.mypage.dto.UserReviewsResponse;
import com.yong2gether.ywave.mypage.dto.ReviewItemDto;
import com.yong2gether.ywave.review.dto.ReviewRequest;
import com.yong2gether.ywave.mypage.dto.CreateReviewResponse;
import com.yong2gether.ywave.mypage.service.ReviewQueryService;
import com.yong2gether.ywave.review.service.ReviewService;
import com.yong2gether.ywave.user.repository.UserRepository;
import com.yong2gether.ywave.review.domain.Review;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MyPageReviewController {

    private final ReviewQueryService reviewQueryService;
    private final UserRepository userRepository;
    private final ReviewService reviewService;

    // 테스트용 엔드포인트 추가
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("MyPageReviewController is working!");
    }

    @Operation(
            summary = "내가 쓴 리뷰 조회",
            description = "내부 인증된 사용자 기준으로 자신이 작성한 모든 리뷰를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/reviews") // API URL : GET /api/v1/mypage/reviews
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserReviewsResponse> getMyReviews(
            Authentication authentication) {
        String email = (authentication != null ? authentication.getName() : null);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 필요");
        }
        
        Long userId = userRepository.findByEmail(email)
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다."));

        System.out.println("[DEBUG] reviews principal=" + email + ", userId=" + userId);
        List<ReviewItemDto> items = reviewQueryService.getUserReviews(userId);
        return ResponseEntity.ok(
                new UserReviewsResponse("내가 작성한 리뷰 목록 조회에 성공했습니다.", items)
        );
    }

    @Operation(
            summary = "리뷰 작성",
            description = "내부 인증된 사용자 기준으로 특정 가맹점에 리뷰를 작성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/reviews",
        produces = "application/json",
        consumes = "application/json"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CreateReviewResponse> createReview(
            Authentication authentication,
            @RequestParam Long storeId,
            @RequestBody ReviewRequest request
    ) {
        System.out.println("[DEBUG] createReview method called with storeId: " + storeId);
        
        // 프로필/북마크와 동일한 인증 패턴 적용
        String email = (authentication != null ? authentication.getName() : null);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 필요");
        }
        
        Long userId = userRepository.findByEmail(email)
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다."));

        System.out.println("[DEBUG] createReview principal=" + email + ", userId=" + userId + ", storeId=" + storeId);

        // 리뷰 작성 서비스 호출
        Review review = reviewService.createReviewWithStoreId(userId, storeId, request);
        
        return ResponseEntity.ok(CreateReviewResponse.success(
            review.getId(),
            request.getRating(),
            request.getContent(),
            request.getImgUrls()
        ));
    }

    @Operation(
            summary = "리뷰 수정",
            description = "내부 인증된 사용자 기준으로 자신이 작성한 리뷰를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "리뷰 없음")
    })
    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CreateReviewResponse> updateReview(
            Authentication authentication,
            @PathVariable Long reviewId,
            @RequestBody ReviewRequest request
    ) {
        String email = (authentication != null ? authentication.getName() : null);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 필요");
        }

        Long userId = userRepository.findByEmail(email)
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다."));

        Review updated = reviewService.updateReview(userId, reviewId, request);

        return ResponseEntity.ok(CreateReviewResponse.success(
                updated.getId(),
                updated.getRating(),
                updated.getContent(),
                updated.getImgUrls()
        ));
    }
}
