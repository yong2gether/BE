package com.yong2gether.ywave.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DeleteReviewResponse")
public record DeleteReviewResponse(
        @Schema(example = "리뷰가 성공적으로 삭제되었습니다.")
        String message
) {}
