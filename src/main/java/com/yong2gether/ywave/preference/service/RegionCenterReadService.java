package com.yong2gether.ywave.preference.service;

import com.yong2gether.ywave.preference.repository.RegionCenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegionCenterReadService {

    private final RegionCenterRepository repo;
    private final GooglePlacesService places;  // ✅ Places 사용

    /**
     * dongForLookup == null 이면 시군구 대표 좌표를 찾는다.
     * DB에 없으면 Places API로 조회 → DB에 UPSERT 캐시 후 반환.
     */
    @Transactional
    public Optional<RegionCenterRepository.LatLng> resolve(String sido, String sigungu, String dongForLookup) {
        // 1) exact (dong 포함)
        var exact = repo.findCenter(sido, sigungu, dongForLookup);
        if (exact.isPresent()) return exact;

        // 2) 시군구 대표 (dong='전체' 우선)
        var bySigungu = repo.findCenterBySigungu(sido, sigungu);
        if (bySigungu.isPresent()) return bySigungu;

        // 3) Places 폴백
        var fromPlaces = places.geocodeByPlaces(sido, sigungu, dongForLookup);
        if (fromPlaces.isPresent()) {
            var p = fromPlaces.get();
            // 저장 시: dongForLookup == null이면 '전체'로 저장(대표 좌표)
            String dongForStorage = (dongForLookup == null ? "전체" : dongForLookup);
            repo.upsert(sido, sigungu, dongForStorage, p.lat(), p.lng());

            return Optional.of(new RegionCenterRepository.LatLng() {
                public Double getLat() { return p.lat(); }
                public Double getLng() { return p.lng(); }
            });
        }

        return Optional.empty();
    }
}
