-- V1__create_store_place_mapping.sql
-- default-schema가 core 이므로 스키마 접두어 없이 작성
CREATE TABLE IF NOT EXISTS store_place_mapping (
                                                   id           BIGSERIAL PRIMARY KEY,
                                                   store_id     BIGINT NOT NULL,
                                                   place_id     VARCHAR(128) NOT NULL,
    confidence   DOUBLE PRECISION NOT NULL DEFAULT 0.8,
    matched_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_store_place__store
    FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE,
    CONSTRAINT uk_store_place UNIQUE (store_id, place_id)
    );

CREATE INDEX IF NOT EXISTS idx_store_place__store_id ON store_place_mapping(store_id);
CREATE INDEX IF NOT EXISTS idx_store_place__place_id ON store_place_mapping(place_id);
