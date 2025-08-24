package com.yong2gether.ywave.preference.service;

import com.yong2gether.ywave.preference.repository.RegionCenterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegionCenterReadService {

    private final RegionCenterRepository repo;
    private final GooglePlacesService places;  // Places 사용

    /**
     * dongForLookup == null 이면 시군구 대표 좌표를 찾는다.
     * DB에 없으면 Places API로 조회 → DB에 UPSERT 캐시 후 반환.
     */
    @Transactional
    public Optional<RegionCenterRepository.LatLng> resolve(String sido, String sigungu, String dongForLookup) {
        // 0) 입력 로그
        log.info("[resolve] sido={}, sigungu={}, dongForLookup={}", sido, sigungu, dongForLookup);

        // 1) exact
        var exact = repo.findCenter(sido, sigungu, dongForLookup);
        if (exact.isPresent()) { log.info("[resolve] hit exact"); return exact; }

        // 2) by sigungu (대표좌표)
        var bySigungu = repo.findCenterBySigungu(sido, sigungu);
        if (bySigungu.isPresent()) { log.info("[resolve] hit bySigungu"); return bySigungu; }

        // 3) loose exact
        var loose = repo.findCenterLoose(sido, sigungu, dongForLookup);
        if (loose.isPresent()) { log.info("[resolve] hit loose(dong)"); return loose; }

        // 4) loose sigungu
        var looseSigungu = repo.findCenterBySigunguLoose(sido, sigungu);
        if (looseSigungu.isPresent()) { log.info("[resolve] hit loose(sigungu)"); return looseSigungu; }

        // 5) Places 폴백: 시군구+동 → 시군구 → 시도
        var fromPlaces = places.geocodeByPlaces(sido, sigungu, dongForLookup);
        if (fromPlaces.isEmpty() && sigungu != null) {
            log.info("[resolve] places miss; retry sigungu-only");
            fromPlaces = places.geocodeByPlaces(sido, sigungu, null);
        }
        if (fromPlaces.isEmpty()) {
            log.info("[resolve] places miss; retry sido-only");
            fromPlaces = places.geocodeByPlaces(sido, null, null);
        }
        if (fromPlaces.isPresent()) {
            var p = fromPlaces.get();
            String dongForStorage = (dongForLookup == null ? "전체" : dongForLookup);
            if (sigungu != null) {
                repo.upsert(sido, sigungu, dongForStorage, p.lat(), p.lng());
                log.info("[resolve] places upserted cache: {}, {}, {}", sido, sigungu, dongForStorage);
            } else {
                log.info("[resolve] places hit(sido-only), not cached due to unique key");
            }
            var lat = p.lat(); var lng = p.lng();
            return Optional.of(new RegionCenterRepository.LatLng() {
                public Double getLat() { return lat; }
                public Double getLng() { return lng; }
            });
        }

        log.warn("[resolve] all sources failed");
        return Optional.empty();
    }
}
