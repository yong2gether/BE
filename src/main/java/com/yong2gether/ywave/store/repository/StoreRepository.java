package com.yong2gether.ywave.store.repository;

import com.yong2gether.ywave.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    interface NearbyRow {
        Long getId();
        String getName();
        String getSigungu();
        Double getLng();
        Double getLat();
        Double getDistM();
        String getPlaceId();
    }

    // language=PostgreSQL
//noinspection SqlResolve,SqlNoDataSourceInspection
    @Query(value = """
  SELECT s.id, s.name, s.sigungu,
         ST_X(s.geom) AS lng,
         ST_Y(s.geom) AS lat,
         ST_Distance(s.geom::geography,
                     ST_SetSRID(ST_MakePoint(:lng,:lat),4326)::geography) AS "distM",
         sp.place_id AS "placeId"
  FROM core.stores s
  LEFT JOIN core.store_place_mapping sp ON sp.store_id = s.id
  WHERE ST_DWithin(s.geom::geography,
                   ST_SetSRID(ST_MakePoint(:lng,:lat),4326)::geography,
                   :radius)
    AND (:q IS NULL
         OR s.name ILIKE CONCAT('%',:q,'%')
         OR s.sector_raw ILIKE CONCAT('%',:q,'%')
         OR s.main_prd_raw ILIKE CONCAT('%',:q,'%'))
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
}
