// src/main/java/com/yong2gether/ywave/mypage/dto/ReviewItemDto.java
package com.yong2gether.ywave.mypage.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yong2gether.ywave.review.repository.projection.ReviewListItemView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "ReviewItem")
public record ReviewItemDto(
        @Schema(example = "1") Long reviewId,
        @Schema(example = "123") Long storeId,
        @Schema(example = "커피 맛집!") String content,
        @Schema(example = "4.5") Double rating,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")          // API 명세서와 동일
        @Schema(example = "2025-08-04T14:21:00")
        LocalDateTime createdAt
) {
    public static ReviewItemDto from(ReviewListItemView v) {
        return new ReviewItemDto(
                v.getReviewId(),
                v.getStoreId(),
                v.getContent(),
                v.getRating(),
                v.getCreatedAt()
        );
    }
}
