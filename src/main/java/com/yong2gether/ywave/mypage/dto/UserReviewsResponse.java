package com.yong2gether.ywave.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "UserReviewsResponse")
public record UserReviewsResponse(
        @Schema(example = "사용자가 작성한 리뷰 목록 조회에 성공했습니다.")
        String message,
        List<ReviewItemDto> reviews
) {}
