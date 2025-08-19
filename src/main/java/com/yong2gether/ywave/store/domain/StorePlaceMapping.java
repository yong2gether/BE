package com.yong2gether.ywave.store.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "store_place_mapping", schema = "core",
        uniqueConstraints = @UniqueConstraint(name = "uk_store_place", columnNames = {"store_id","place_id"}))
public class StorePlaceMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → stores.id
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    // Google Places place_id
    @Column(name = "place_id", nullable = false, length = 128)
    private String placeId;

    // 매칭 신뢰도(0~1)
    @Column(name = "confidence", nullable = false)
    private Double confidence;

    @Column(name = "matched_at", nullable = false)
    private OffsetDateTime matchedAt;

    public StorePlaceMapping() {}

    public StorePlaceMapping(Long storeId, String placeId, Double confidence, OffsetDateTime matchedAt) {
        this.storeId = storeId;
        this.placeId = placeId;
        this.confidence = confidence;
        this.matchedAt = matchedAt;
    }

    public Long getId() { return id; }
    public Long getStoreId() { return storeId; }
    public String getPlaceId() { return placeId; }
    public Double getConfidence() { return confidence; }
    public OffsetDateTime getMatchedAt() { return matchedAt; }

    public void setId(Long id) { this.id = id; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public void setMatchedAt(OffsetDateTime matchedAt) { this.matchedAt = matchedAt; }
}
