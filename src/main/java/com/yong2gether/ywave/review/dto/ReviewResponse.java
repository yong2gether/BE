package com.yong2gether.ywave.review.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
public class ReviewResponse {
    private Long reviewId;
    private String message;
    private Double rating;
    private String content;
    private List<String> imgUrls;
}
