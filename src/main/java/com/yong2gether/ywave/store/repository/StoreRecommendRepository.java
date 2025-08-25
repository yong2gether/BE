package com.yong2gether.ywave.store.repository;

import com.yong2gether.ywave.store.domain.Store;
import com.yong2gether.ywave.store.repository.projection.StorePickRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreRecommendRepository extends JpaRepository<Store, Long> {

    @Query(value = """
        WITH filtered AS (
          SELECT
            s.id, s.name, s.sigungu, s.road_addr,
            ST_X(s.geom) AS lng,
            ST_Y(s.geom) AS lat,
            COALESCE(s.popularity_score, 0) AS popularity_score,
            COALESCE(s.category, s.category_ai, '기타') AS effective_category
          FROM core.stores s
          WHERE ST_DWithin(
                  s.geog,
                  ST_SetSRID(ST_MakePoint(:centerLng, :centerLat), 4326)::geography,
                  :radiusMeters
                )
        )
        SELECT id AS id, name AS name, sigungu AS sigungu, road_addr AS roadAddr,
               lng AS lng, lat AS lat, popularity_score AS popularityScore
        FROM filtered
        WHERE (:namesCount = 0 OR effective_category = ANY(:categoryNames))
        ORDER BY -LN(random()) / GREATEST(popularity_score, 0.05)
        LIMIT :limit
        """, nativeQuery = true)
    List<StorePickRow> pickWeightedRandomByNames(
            @Param("centerLng") double centerLng,
            @Param("centerLat") double centerLat,
            @Param("radiusMeters") int radiusMeters,
            @Param("categoryNames") String[] categoryNames,
            @Param("namesCount") int namesCount,
            @Param("limit") int limit
    );

    // (옵션) ID 기반을 계속 써야 한다면 유지. 하지만 AI 추천이 '효과 카테고리'를 쓰는 게 목적이라면
    // 서비스단에서 이 메서드는 더 이상 호출하지 않도록 권장.
    @Query(value = """
        WITH filtered AS (
          SELECT
            s.id, s.name, s.sigungu, s.road_addr,
            ST_X(s.geom) AS lng, ST_Y(s.geom) AS lat,
            COALESCE(s.popularity_score, 0) AS popularity_score
          FROM core.stores s
          WHERE ST_DWithin(
                  s.geog,
                  ST_SetSRID(ST_MakePoint(:centerLng, :centerLat), 4326)::geography,
                  :radiusMeters
                )
        )
        SELECT id AS id, name AS name, sigungu AS sigungu, road_addr AS roadAddr,
               lng AS lng, lat AS lat, popularity_score AS popularityScore
        FROM filtered
        ORDER BY -LN(random()) / GREATEST(popularity_score, 0.05)
        LIMIT :limit
        """, nativeQuery = true)
    List<StorePickRow> pickWeightedRandomByIds_NoCategoryJoin(
            @Param("centerLng") double centerLng,
            @Param("centerLat") double centerLat,
            @Param("radiusMeters") int radiusMeters,
            @Param("limit") int limit
    );
}
