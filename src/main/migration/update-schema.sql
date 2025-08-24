DROP TABLE public.spatial_ref_sys CASCADE;

CREATE INDEX ix_category_name ON category (name);

ALTER TABLE core.preference_category
    DROP CONSTRAINT fk5a18pyes6fhrdthp8y8lpirps;

ALTER TABLE core.store_place_mapping
    DROP CONSTRAINT fk_store_place__store;

ALTER TABLE core.preference_category
    DROP CONSTRAINT fkrg63vi0tokyexkwhj9wthvdso;

ALTER TABLE core.store_ai_labels
    DROP CONSTRAINT store_ai_labels_store_id_fkey;

ALTER TABLE core.store_media
    DROP CONSTRAINT store_media_store_id_fkey;

ALTER TABLE core.store_places
    DROP CONSTRAINT store_places_store_id_fkey;

CREATE TABLE core.store_category
(
    store_id    BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    subcategory VARCHAR(120),
    CONSTRAINT pk_store_category PRIMARY KEY (store_id)
);

ALTER TABLE core.store_category
    ADD CONSTRAINT FK_STORE_CATEGORY_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category (id);

CREATE INDEX ix_store_category_category_id ON core.store_category (category_id);

DROP TABLE core.category CASCADE;

DROP TABLE core.preference_category CASCADE;

DROP TABLE core.store_ai_labels CASCADE;

DROP TABLE core.store_media CASCADE;

DROP TABLE core.store_places CASCADE;

DROP TABLE core.users CASCADE;

ALTER TABLE core.stores
    DROP COLUMN addr_norm;

ALTER TABLE core.stores
    DROP COLUMN address;

ALTER TABLE core.stores
    DROP COLUMN category_id;

ALTER TABLE core.stores
    DROP COLUMN created_at;

ALTER TABLE core.stores
    DROP COLUMN geog;

ALTER TABLE core.stores
    DROP COLUMN is_active;

ALTER TABLE core.stores
    DROP COLUMN latitude;

ALTER TABLE core.stores
    DROP COLUMN longitude;

ALTER TABLE core.stores
    DROP COLUMN name_norm;

ALTER TABLE core.stores
    DROP COLUMN phone_number;

ALTER TABLE core.stores
    DROP COLUMN source_row_id;

ALTER TABLE core.stores
    DROP COLUMN store_name;

ALTER TABLE core.stores
    DROP COLUMN updated_at;

ALTER TABLE core.stores
    ALTER COLUMN lotno_addr TYPE VARCHAR(255) USING (lotno_addr::VARCHAR(255));

ALTER TABLE core.stores
    ALTER COLUMN main_prd_raw TYPE VARCHAR(255) USING (main_prd_raw::VARCHAR(255));

ALTER TABLE core.stores
    ALTER COLUMN name TYPE VARCHAR(255) USING (name::VARCHAR(255));

ALTER TABLE core.stores
    ALTER COLUMN phone TYPE VARCHAR(255) USING (phone::VARCHAR(255));

ALTER TABLE core.stores
    ALTER COLUMN road_addr TYPE VARCHAR(255) USING (road_addr::VARCHAR(255));

ALTER TABLE core.stores
    ALTER COLUMN sector_raw TYPE VARCHAR(255) USING (sector_raw::VARCHAR(255));

ALTER TABLE core.stores
    ALTER COLUMN sido TYPE VARCHAR(255) USING (sido::VARCHAR(255));

ALTER TABLE core.stores
    ALTER COLUMN sigungu TYPE VARCHAR(255) USING (sigungu::VARCHAR(255));