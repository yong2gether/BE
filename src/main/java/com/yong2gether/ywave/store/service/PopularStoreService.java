package com.yong2gether.ywave.store.service;

import com.yong2gether.ywave.store.dto.PopularStoreDto;
import com.yong2gether.ywave.store.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PopularStoreService {

    private final StoreRepository storeRepository;

    public PopularStoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<PopularStoreDto> popular(double lng, double lat, int radius, int limit,
                                         List<String> categories, String q) {

        String[] cats = (categories == null || categories.isEmpty())
                ? new String[]{} : categories.toArray(new String[0]);

        boolean useCatFilter = cats.length > 0;                 // 필터 사용 여부

        String keyword = (q == null || q.isBlank()) ? null : q;

        return storeRepository
                .findPopularByCategories(lng, lat, radius, limit, useCatFilter, cats, keyword)
                .stream()
                .map(r -> {
                    PopularStoreDto d = new PopularStoreDto();
                    d.setId(r.getId());
                    d.setName(r.getName());
                    d.setSigungu(r.getSigungu());
                    d.setLng(r.getLng());
                    d.setLat(r.getLat());
                    d.setDistM(r.getDistM());
                    d.setPlaceId(r.getPlaceId());
                    // 새 스키마 매핑
                    d.setRating(r.getRating());
                    d.setUserRatingsTotal(r.getUserRatingsTotal());
                    d.setPopularityScore(r.getPopularity());
                    d.setCategory(r.getCategory());
                    return d;
                }).toList();
    }

}
