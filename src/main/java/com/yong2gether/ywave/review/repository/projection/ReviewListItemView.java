package com.yong2gether.ywave.review.repository.projection;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewListItemView {
    Long getReviewId();
    Long getStoreId();
    String getContent();
    Double getRating();
    LocalDateTime getCreatedAt();
    List<String> getImgUrls();
}