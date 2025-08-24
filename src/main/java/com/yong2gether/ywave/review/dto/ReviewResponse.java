package com.yong2gether.ywave.review.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ReviewResponse {
    private Long reviewId;
    private String message;
    private Double rating;
    private String content;
    private List<String> imageUrls;
}
