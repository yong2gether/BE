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

    @Transactional
    public ReviewResponse createReview(Long userId, ReviewRequest request) {
        // 사용자 정보 조회 및 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 임시: storeId 1번 가맹점 사용 (기존 로직 유지)
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
        // 사용자 정보 조회 및 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // storeId 유효성 검증 추가
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
}
