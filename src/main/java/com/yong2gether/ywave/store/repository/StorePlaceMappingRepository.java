package com.yong2gether.ywave.store.repository;

import com.yong2gether.ywave.store.domain.StorePlaceMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface StorePlaceMappingRepository extends JpaRepository<StorePlaceMapping, Long> {
    Optional<StorePlaceMapping> findFirstByStoreIdOrderByConfidenceDesc(Long storeId);

    Optional<StorePlaceMapping> findFirstByPlaceId(String placeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """

            UPDATE core.store_place_mapping
           SET rating = :rating,
               review_count = :reviewCount,
               synced_at = :syncedAt
         WHERE place_id = :placeId
        """, nativeQuery = true)
    int updatePopularityByPlaceId(@Param("placeId") String placeId,
                                  @Param("rating") Double rating,
                                  @Param("reviewCount") Integer reviewCount,
                                  @Param("syncedAt") OffsetDateTime syncedAt);
}
