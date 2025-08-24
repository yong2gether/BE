package com.yong2gether.ywave.review.controller;

import com.yong2gether.ywave.auth.userdetails.CustomUserDetails;
import com.yong2gether.ywave.review.dto.ReviewRequest;
import com.yong2gether.ywave.review.dto.ReviewResponse;
import com.yong2gether.ywave.review.dto.ReviewUpdateRequest;
import com.yong2gether.ywave.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


@RestController
@RequestMapping("/api/v1/mypage/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PutMapping("/{reviewId}")
    public ReviewResponse updateReview(
            @PathVariable long reviewId,
            @RequestBody ReviewUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        long userId = userDetails.getId();
        return reviewService.updateReview(userId, reviewId, request);
    }

}
