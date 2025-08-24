package com.yong2gether.ywave.preference.repository;

import com.yong2gether.ywave.preference.domain.RegionCenter;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RegionCenterRepository extends JpaRepository<RegionCenter, Long> {
    interface LatLng {
        Double getLat();
        Double getLng();
    }

    @Query(value = """
        SELECT lat, lng
        FROM core.region_center
        WHERE sido = :sido
          AND sigungu = :sigungu
          AND ((:dong IS NULL AND dong IS NULL) OR dong = :dong)
        LIMIT 1
        """, nativeQuery = true)
    Optional<LatLng> findCenter(@Param("sido") String sido,
                                @Param("sigungu") String sigungu,
                                @Param("dong") String dong);

    // dong='전체'가 있으면 우선 반환 (대표 좌표)
    @Query(value = """
        SELECT lat, lng
        FROM core.region_center
        WHERE sido = :sido
          AND sigungu = :sigungu
        ORDER BY (dong = '전체') DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<LatLng> findCenterBySigungu(@Param("sido") String sido,
                                         @Param("sigungu") String sigungu);

    // 캐시 저장
    @Modifying
    @Query(value = """
        INSERT INTO core.region_center (sido, sigungu, dong, lat, lng)
        VALUES (:sido, :sigungu, :dong, :lat, :lng)
        ON CONFLICT (sido, sigungu, dong)
        DO UPDATE SET lat = EXCLUDED.lat, lng = EXCLUDED.lng
        """, nativeQuery = true)
    void upsert(@Param("sido") String sido,
                @Param("sigungu") String sigungu,
                @Param("dong") String dong,
                @Param("lat") Double lat,
                @Param("lng") Double lng);
}
