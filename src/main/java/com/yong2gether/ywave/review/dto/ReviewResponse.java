package com.yong2gether.ywave.review.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewResponse {
    private Long reviewId;
    private String message;
    private Double rating;
    private String content;
}
