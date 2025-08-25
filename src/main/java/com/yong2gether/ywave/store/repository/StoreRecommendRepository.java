package com.yong2gether.ywave.store.repository;

import com.yong2gether.ywave.store.domain.Store;
import com.yong2gether.ywave.store.repository.projection.StorePickRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreRecommendRepository extends JpaRepository<Store, Long> {

    // (A) category_id OR + 반경 필터
    @Query(value = """
        WITH filtered AS (
          SELECT
            s.id, s.name, s.sigungu, s.road_addr,
            ST_X(s.geom) AS lng, ST_Y(s.geom) AS lat,
            COALESCE(s.popularity_score, 0) AS popularity_score
          FROM core.stores s
          JOIN core.store_category sc ON sc.store_id = s.id
          WHERE ST_DWithin(
                  s.geog,
                  ST_SetSRID(ST_MakePoint(:centerLng, :centerLat), 4326)::geography,
                  :radiusMeters
                )
            AND ( :categoryIdsCount = 0 OR sc.category_id = ANY(:categoryIds) )
        )
        SELECT id AS id, name AS name, sigungu AS sigungu, road_addr AS roadAddr,
               lng AS lng, lat AS lat, popularity_score AS popularityScore
        FROM filtered
        ORDER BY -LN(random()) / GREATEST(popularity_score, 0.05)
        LIMIT :limit
        """, nativeQuery = true)
    List<StorePickRow> pickWeightedRandomByIds(
            @Param("centerLng") double centerLng,
            @Param("centerLat") double centerLat,
            @Param("radiusMeters") int radiusMeters,
            @Param("categoryIds") Long[] categoryIds,
            @Param("categoryIdsCount") int categoryIdsCount,
            @Param("limit") int limit
    );

    // category.name OR + 반경 필터 (이름으로 직접 필터)
    @Query(value = """
        WITH filtered AS (
          SELECT
            s.id, s.name, s.sigungu, s.road_addr,
            ST_X(s.geom) AS lng, ST_Y(s.geom) AS lat,
            COALESCE(s.popularity_score, 0) AS popularity_score
          FROM core.stores s
          JOIN core.store_category sc ON sc.store_id = s.id
          JOIN core.category c ON c.id = sc.category_id
          WHERE ST_DWithin(
                  s.geog,
                  ST_SetSRID(ST_MakePoint(:centerLng, :centerLat), 4326)::geography,
                  :radiusMeters
                )
            AND ( :namesCount = 0 OR c.name = ANY(:categoryNames) )
        )
        SELECT id AS id, name AS name, sigungu AS sigungu, road_addr AS roadAddr,
               lng AS lng, lat AS lat, popularity_score AS popularityScore
        FROM filtered
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
}
