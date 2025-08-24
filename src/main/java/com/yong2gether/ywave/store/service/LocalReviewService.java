package com.yong2gether.ywave.store.service;

import com.yong2gether.ywave.store.dto.PlaceDetailsDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocalReviewService {
    public List<PlaceDetailsDto.Review> getReviewsForStore(Long storeId) {
        return List.of();
    }

}
