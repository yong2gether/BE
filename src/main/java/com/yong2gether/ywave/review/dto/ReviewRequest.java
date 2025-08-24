package com.yong2gether.ywave.review.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {
    private Double rating;
    private String content;
}
