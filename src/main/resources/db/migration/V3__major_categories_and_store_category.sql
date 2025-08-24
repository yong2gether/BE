DO $$
    DECLARE
        vals TEXT[] := ARRAY[
            'FOOD','CAFE','MOVIE_SHOW','MEDICAL','MART_SUPER','EDUCATION_STATIONERY',
            'LODGING','LIVING_CONVENIENCE','APPAREL_MISC','SPORTS','GAS_STATION','ETC'
            ];
        v TEXT;
    BEGIN
        FOREACH v IN ARRAY vals LOOP
                INSERT INTO core.category(name)
                SELECT v WHERE NOT EXISTS (SELECT 1 FROM core.category WHERE name = v);
            END LOOP;
    END$$;

CREATE TABLE IF NOT EXISTS core.store_category (
                                                   store_id    BIGINT PRIMARY KEY REFERENCES core.stores(id) ON DELETE CASCADE,
                                                   category_id BIGINT NOT NULL    REFERENCES core.category(id) ON DELETE RESTRICT,
                                                   subcategory VARCHAR(120)
);
CREATE INDEX IF NOT EXISTS idx_store_category__category_id
    ON core.store_category(category_id);
