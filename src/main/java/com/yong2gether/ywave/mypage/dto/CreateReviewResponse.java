package com.yong2gether.ywave.mypage.dto;

import java.util.List;

public record CreateReviewResponse(
    Long reviewId,
    String message,
    Double rating,
    String content,
    List<String> imgUrls
) {
    public static CreateReviewResponse success(Long reviewId, Double rating, String content, List<String> imgUrls) {
        return new CreateReviewResponse(
            reviewId,
            "리뷰가 작성되었습니다.",
            rating,
            content,
            imgUrls
        );
    }
}
