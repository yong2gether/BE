package com.yong2gether.ywave.store.repository;

import com.yong2gether.ywave.store.domain.StorePlaceMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StorePlaceMappingRepository extends JpaRepository<StorePlaceMapping, Long> {
    Optional<StorePlaceMapping> findFirstByStoreIdOrderByConfidenceDesc(Long storeId);
    Optional<StorePlaceMapping> findFirstByPlaceId(String placeId);
}
