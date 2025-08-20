package com.yong2gether.ywave.store.repository;

import com.yong2gether.ywave.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    // ---- Projections ----
    interface NearbyRow {
        Long getId();
        String getName();
        String getSigungu();
        Double getLng();
        Double getLat();
        Double getDistM();
        String getPlaceId();
        String getCategory();          // = category_ai (COALESCE로 '기타')
        Double getRating();            // COALESCE로 0
        Integer getUserRatingsTotal(); // COALESCE로 0
    }

    interface PopularRow {
        Long getId();
        String getName();
        String getSigungu();
        Double getLng();
        Double getLat();
        Double getDistM();
        String getPlaceId();
        String getCategory();          // = category_ai (COALESCE로 '기타')
        Double getRating();            // COALESCE로 0
        Integer getUserRatingsTotal(); // COALESCE로 0
        Double getPopularity();        // = popularity_score (COALESCE로 0)
    }

    // -------------------- NEARBY (거리순) --------------------
    @Query(value = """
        SELECT
          s.id,
          s.name,
          s.sigungu,
          ST_X(s.geom) AS lng,
          ST_Y(s.geom) AS lat,
          ST_Distance(
            s.geog,
            ST_SetSRID(ST_MakePoint(:lng,:lat),4326)::geography
          ) AS "distM",
          s.place_id                                      AS "placeId",
          COALESCE(s.category_ai, '기타')                 AS "category",
          COALESCE(s.rating, 0)                           AS rating,
          COALESCE(s.user_ratings_total, 0)               AS "userRatingsTotal"
        FROM core.stores s
        WHERE ST_DWithin(
            s.geog,
            ST_SetSRID(ST_MakePoint(:lng,:lat),4326)::geography,
            :radius
          )
          AND (
            :q IS NULL
            OR s.name ILIKE CONCAT('%', :q, '%')
            OR s.category_ai ILIKE CONCAT('%', :q, '%')
          )
        ORDER BY "distM" ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<NearbyRow> findNearby(
            @Param("lng") double lng,
            @Param("lat") double lat,
            @Param("radius") int radius,
            @Param("limit") int limit,
            @Param("q") String q
    );

    // -------------------- POPULAR (인기순) --------------------
    // :useCatFilter 로 필터 사용 여부를 제어 (배열 파라미터는 1회만 사용)
    @Query(value = """
        SELECT
          s.id,
          s.name,
          s.sigungu,
          ST_X(s.geom) AS lng,
          ST_Y(s.geom) AS lat,
          ST_Distance(
            s.geog,
            ST_SetSRID(ST_MakePoint(:lng,:lat),4326)::geography
          ) AS "distM",
          s.place_id                                      AS "placeId",
          COALESCE(s.category_ai, '기타')                 AS "category",
          COALESCE(s.rating, 0)                           AS rating,
          COALESCE(s.user_ratings_total, 0)               AS "userRatingsTotal",
          COALESCE(s.popularity_score, 0)                 AS "popularity"
        FROM core.stores s
        WHERE ST_DWithin(
            s.geog,
            ST_SetSRID(ST_MakePoint(:lng,:lat),4326)::geography,
            :radius
          )
          AND (
            :useCatFilter = FALSE
            OR s.category_ai = ANY(:categories)                         -- 명시 카테고리
            OR ('기타' = ANY(:categories) AND s.category_ai IS NULL)    -- '기타' 선택 시 NULL 포함
          )
          AND (
            :q IS NULL
            OR s.name ILIKE CONCAT('%', :q, '%')
            OR s.category_ai ILIKE CONCAT('%', :q, '%')
          )
        ORDER BY
          s.popularity_score   DESC NULLS LAST,
          s.rating             DESC NULLS LAST,
          s.user_ratings_total DESC NULLS LAST,
          "distM" ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<PopularRow> findPopularByCategories(
            @Param("lng") double lng,
            @Param("lat") double lat,
            @Param("radius") int radius,
            @Param("limit") int limit,
            @Param("useCatFilter") boolean useCatFilter,
            @Param("categories") String[] categories,
            @Param("q") String q
    );

    // -------------------- 분류 상태 점검 --------------------
    @Query(value = """
        SELECT COUNT(*)
        FROM core.stores s
        WHERE s.category_ai IS NULL
        """, nativeQuery = true)
    long countUncategorized();

    // 미분류 배치(키셋)
    @Query(value = """
        SELECT s.*
        FROM core.stores s
        WHERE s.id > :afterId
          AND s.category_ai IS NULL
        ORDER BY s.id
        LIMIT :limit
        """, nativeQuery = true)
    List<Store> findUncategorizedAfter(@Param("afterId") long afterId,
                                       @Param("limit") int limit);

    // ---- Signature 유틸 (정규화된 이름/주소 기반) ----
    interface SignatureRow {
        String getSig();
        Long getCnt();
    }

    @Query(value = """
        SELECT lower(coalesce(s.name_norm, s.name, '') || '|' || coalesce(s.addr_norm, s.road_addr, '')) AS sig,
               count(*) AS cnt
        FROM core.stores s
        WHERE s.category_ai IS NULL
        GROUP BY 1
        ORDER BY cnt DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<SignatureRow> findUncategorizedSignatures(@Param("limit") int limit);

    @Query(value = """
        SELECT s.*
        FROM core.stores s
        WHERE s.category_ai IS NULL
          AND lower(coalesce(s.name_norm, s.name, '') || '|' || coalesce(s.addr_norm, s.road_addr, '')) = :sig
        ORDER BY s.id
        LIMIT :limit
        """, nativeQuery = true)
    List<Store> findStoresBySignature(@Param("sig") String sig, @Param("limit") int limit);
}
