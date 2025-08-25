package com.yong2gether.ywave.review.repository.projection;

import java.time.LocalDateTime;

public interface ReviewListItemView {
    Long getReviewId();
    Long getStoreId();
    String getContent();
    Double getRating();
    LocalDateTime getCreatedAt();
}