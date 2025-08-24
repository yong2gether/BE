-- 인기 지표(구글) - 기존 매핑 테이블에 칼럼 추가
ALTER TABLE core.store_place_mapping
    ADD COLUMN IF NOT EXISTS rating       DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS review_count INTEGER,
    ADD COLUMN IF NOT EXISTS synced_at    TIMESTAMPTZ;
