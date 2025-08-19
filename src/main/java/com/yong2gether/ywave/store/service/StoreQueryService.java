package com.yong2gether.ywave.store.service;

import com.yong2gether.ywave.store.dto.NearbyStoreDto;
import com.yong2gether.ywave.store.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreQueryService {

    private final StoreRepository storeRepository;

    public StoreQueryService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<NearbyStoreDto> findNearby(double lng, double lat, int radius, int limit, String q) {
        var rows = storeRepository.findNearby(lng, lat, radius, limit, (q == null || q.isBlank()) ? null : q);
        return rows.stream()
                .map(r -> new NearbyStoreDto(
                        r.getId(), r.getName(), r.getSigungu(),
                        r.getLng(), r.getLat(), r.getDistM(), r.getPlaceId()
                ))
                .toList();
    }
}
