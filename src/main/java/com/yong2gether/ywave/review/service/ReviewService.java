package com.yong2gether.ywave.review.service;

import com.yong2gether.ywave.review.domain.Review;
import com.yong2gether.ywave.review.dto.ReviewRequest;
import com.yong2gether.ywave.review.dto.ReviewResponse;
import com.yong2gether.ywave.review.repository.ReviewRepository;
import com.yong2gether.ywave.store.repository.StoreRepository;
import com.yong2gether.ywave.user.domain.User;
import com.yong2gether.ywave.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    // 일반 리뷰 작성 (임시 storeId = 1)
    @Transactional
    public ReviewResponse createReview(Long userId, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Long storeId = 1L; // 임시

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

    // 특정 storeId 지정 리뷰 작성
    @Transactional
    public Review createReviewWithStoreId(Long userId, Long storeId, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

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
