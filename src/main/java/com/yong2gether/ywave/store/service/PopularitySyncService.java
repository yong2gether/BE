package com.yong2gether.ywave.store.service;

import com.yong2gether.ywave.store.dto.PlaceDetailsDto;
import com.yong2gether.ywave.store.repository.StorePlaceMappingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class PopularitySyncService {

    private final StorePlaceMappingRepository mappingRepo;
    private final PlacesClient placesClient;

    public PopularitySyncService(StorePlaceMappingRepository mappingRepo, PlacesClient placesClient) {
        this.mappingRepo = mappingRepo;
        this.placesClient = placesClient;
    }

    // placeId가 있는 매핑 중 일부를 동기화 (간단 버전)
    @Transactional
    public int syncTopN(int limit) {
        var all = mappingRepo.findAll().stream()
                .filter(m -> m.getPlaceId() != null)
                .limit(limit)
                .toList();

        int n = 0;
        for (var m : all) {
            PlaceDetailsDto d = placesClient.getPlaceDetails(m.getPlaceId());
            if (d != null) {
                mappingRepo.updatePopularityByPlaceId(
                        m.getPlaceId(),
                        d.getRating(),
                        d.getReviewCount(),
                        OffsetDateTime.now()
                );
                n++;
            }
        }
        return n;
    }
}
