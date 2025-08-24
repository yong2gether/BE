package com.yong2gether.ywave.review.controller;

import com.yong2gether.ywave.review.dto.ReviewRequest;
import com.yong2gether.ywave.review.dto.ReviewResponse;
import com.yong2gether.ywave.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mypage/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ReviewResponse createReview(
            @RequestBody ReviewRequest request,
            @RequestHeader("X-USER-ID") Long userId // 로그인 유저 ID
    ) {
        return reviewService.createReview(userId, request);
    }
}
