package com.yong2gether.ywave.review.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ReviewRequest {
    @NotNull(message = "평점은 필수입니다.")
    @DecimalMin(value = "0.0", message = "평점은 0.0 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다.")
    private Double rating;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    private String content;

    private List<String> imgUrls;
}