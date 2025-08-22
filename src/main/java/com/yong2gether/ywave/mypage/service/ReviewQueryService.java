package com.yong2gether.ywave.mypage.service;

import com.yong2gether.ywave.mypage.dto.ReviewItemDto;
import com.yong2gether.ywave.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewQueryService {
    private final ReviewRepository reviewRepository;

    public List<ReviewItemDto> getUserReviews(Long userId) {
        return reviewRepository.findAllByUserId(userId)
                .stream().map(ReviewItemDto::from).toList();
    }
}
