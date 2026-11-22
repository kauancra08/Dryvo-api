CREATE TABLE quotes (
    id              BIGSERIAL PRIMARY KEY,
    owner_id        BIGINT          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    client_id       BIGINT          NOT NULL REFERENCES clients (id) ON DELETE RESTRICT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    description     VARCHAR(1000),
    labor_cost      NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    other_cost      NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    discount        NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    subtotal        NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    total           NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT ck_quotes_status
        CHECK (status IN ('DRAFT', 'SENT', 'APPROVED', 'REJECTED', 'CANCELLED')),

    CONSTRAINT ck_quotes_labor_cost
        CHECK (labor_cost >= 0),

    CONSTRAINT ck_quotes_other_cost
        CHECK (other_cost >= 0),

    CONSTRAINT ck_quotes_discount
        CHECK (discount >= 0),

    CONSTRAINT ck_quotes_subtotal
        CHECK (subtotal >= 0),

    CONSTRAINT ck_quotes_total
        CHECK (total >= 0)
);

CREATE INDEX idx_quotes_owner
    ON quotes (owner_id);

CREATE INDEX idx_quotes_owner_client
    ON quotes (owner_id, client_id);

CREATE INDEX idx_quotes_owner_created_at
    ON quotes (owner_id, created_at DESC);


CREATE TABLE quote_items (
    id              BIGSERIAL PRIMARY KEY,
    quote_id        BIGINT          NOT NULL REFERENCES quotes (id) ON DELETE CASCADE,
    material_id     BIGINT          REFERENCES materials (id) ON DELETE SET NULL,
    description     VARCHAR(255)    NOT NULL,
    unit            VARCHAR(20)     NOT NULL,
    quantity        NUMERIC(12, 3)  NOT NULL,
    unit_price      NUMERIC(12, 2)  NOT NULL,
    total           NUMERIC(12, 2)  NOT NULL,

    CONSTRAINT ck_quote_items_unit
        CHECK (unit IN ('UNIT', 'METER', 'SQUARE_METER', 'KG', 'BOX', 'PACK')),

    CONSTRAINT ck_quote_items_quantity
        CHECK (quantity > 0),

    CONSTRAINT ck_quote_items_unit_price
        CHECK (unit_price >= 0),

    CONSTRAINT ck_quote_items_total
        CHECK (total >= 0)
);

CREATE INDEX idx_quote_items_quote
    ON quote_items (quote_id);

CREATE INDEX idx_quote_items_material
    ON quote_items (material_id);