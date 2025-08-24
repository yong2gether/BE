package com.yong2gether.ywave.review.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ReviewUpdateRequest {
    private Double rating;
    private String content;
    private List<String> imageUrls;
}
