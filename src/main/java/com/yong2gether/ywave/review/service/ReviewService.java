package com.yong2gether.ywave.review.service;

import com.yong2gether.ywave.review.domain.Review;
import com.yong2gether.ywave.review.dto.ReviewRequest;
import com.yong2gether.ywave.review.dto.ReviewUpdateRequest;
import com.yong2gether.ywave.review.dto.ReviewResponse;
import com.yong2gether.ywave.review.repository.ReviewRepository;
import com.yong2gether.ywave.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public ReviewResponse createReview(long userId, ReviewRequest request) {
        if (request.getImageUrls() != null && request.getImageUrls().size() > 5) {
            throw new IllegalArgumentException("이미지는 최대 5개까지만 등록 가능합니다.");
        }

        var store = storeRepository.findById(1L) // TODO: 나중에 storeId 동적 처리
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가맹점입니다."));

        Review review = Review.builder()
                .userId(userId)
                .store(store)
                .rating(request.getRating())
                .content(request.getContent())
                .imageUrls(request.getImageUrls())
                .build();

        Review saved = reviewRepository.save(review);

        return ReviewResponse.builder()
                .reviewId(saved.getId())
                .message("리뷰가 작성되었습니다.")
                .rating(saved.getRating())
                .content(saved.getContent())
                .imageUrls(saved.getImageUrls())
                .build();
    }

    @Transactional
    public ReviewResponse updateReview(long userId, long reviewId, ReviewUpdateRequest request) {
        if (request.getImageUrls() != null && request.getImageUrls().size() > 5) {
            throw new IllegalArgumentException("이미지는 최대 5개까지만 등록 가능합니다.");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        if (!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 리뷰만 수정할 수 있습니다.");
        }

        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setImageUrls(request.getImageUrls());

        Review updated = reviewRepository.save(review);

        return ReviewResponse.builder()
                .reviewId(updated.getId())
                .message("리뷰가 수정되었습니다.")
                .rating(updated.getRating())
                .content(updated.getContent())
                .imageUrls(updated.getImageUrls())
                .build();
    }
}
