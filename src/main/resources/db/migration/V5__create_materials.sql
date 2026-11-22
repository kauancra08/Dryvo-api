CREATE TABLE materials (
    id         BIGSERIAL PRIMARY KEY,
    owner_id   BIGINT         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name       VARCHAR(150)   NOT NULL,
    category   VARCHAR(60),
    unit       VARCHAR(20)    NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    supplier   VARCHAR(150),
    notes      VARCHAR(2000),
    created_at TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT ck_materials_unit CHECK (unit IN ('UNIT', 'METER', 'SQUARE_METER', 'KG', 'BOX', 'PACK')),
    CONSTRAINT ck_materials_price CHECK (unit_price >= 0)
);

CREATE INDEX idx_materials_owner ON materials (owner_id);
CREATE INDEX idx_materials_owner_name ON materials (owner_id, lower(name));
