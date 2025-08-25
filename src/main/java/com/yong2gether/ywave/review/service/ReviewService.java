package com.yong2gether.ywave.review.service;

import com.yong2gether.ywave.review.domain.Review;
import com.yong2gether.ywave.review.dto.ReviewRequest;
import com.yong2gether.ywave.review.dto.ReviewResponse;
import com.yong2gether.ywave.review.repository.ReviewRepository;
import com.yong2gether.ywave.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public ReviewResponse createReview(Long userId, ReviewRequest request) {
        // 임시: storeId 1번 가맹점 사용
        Long storeId = 1L;

        Review review = Review.builder()
                .userId(userId)
                .storeId(storeId)
                .rating(request.getRating())
                .content(request.getContent())
                .imgUrls(request.getImgUrls() != null ? request.getImgUrls() : new ArrayList<>())
                .build();

        Review saved = reviewRepository.save(review);

        return ReviewResponse.builder()
                .reviewId(saved.getId())
                .message("리뷰가 작성되었습니다.")
                .rating(saved.getRating())
                .content(saved.getContent())
                .imgUrls(saved.getImgUrls())
                .build();
    }

    @Transactional
    public Review createReviewWithStoreId(Long userId, Long storeId, ReviewRequest request) {
        // storeId 유효성 검증
        if (!storeRepository.existsById(storeId)) {
            throw new RuntimeException("존재하지 않는 가맹점입니다. storeId: " + storeId);
        }
        
        Review review = Review.builder()
                .userId(userId)
                .storeId(storeId)
                .rating(request.getRating())
                .content(request.getContent())
                .imgUrls(request.getImgUrls() != null ? request.getImgUrls() : new ArrayList<>())
                .build();

        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        // 리뷰 존재 여부 및 본인 작성 여부 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 리뷰입니다."));
        
        // 본인이 작성한 리뷰인지 확인
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }
        
        // 리뷰 삭제
        reviewRepository.delete(review);
    }

    // 리뷰 수정
    @Transactional
    public Review updateReview(Long userId, Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다. reviewId: " + reviewId));

        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("작성자만 수정할 수 있습니다.");
        }

        review.setContent(request.getContent());
        review.setRating(request.getRating());
        review.setImgUrls(request.getImgUrls() != null ? request.getImgUrls() : new ArrayList<>());

        return reviewRepository.save(review);
    }
}
